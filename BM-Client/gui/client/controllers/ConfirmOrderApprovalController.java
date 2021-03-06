package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import Entities.Message;
import Entities.MessageType;
import Entities.Order;
import Entities.OrderType;
import Entities.OrdersReport;
import Entities.Receipt;
import Entities.RevenueReport;
import Entities.homeBranches;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import main.ClientUI;
import main.PopUpMessage;

/**
 * @author Aviel This class is for approval of orders awaiting approval.
 */
public class ConfirmOrderApprovalController extends AbstractController implements Initializable {
	public static ArrayList<Order> allOrders = new ArrayList<Order>();
	public static String phoneNumber;
	public static HashMap<String, Integer> dishTypesQuentities = new HashMap<>();
	private String branch;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button Refuse;

	@FXML
	private Button Confirm;

	@FXML
	private Button Send;

	@FXML
	private ImageView backImg;

	@FXML
	private TableView<Order> table;

	@FXML
	private TableColumn<Order, Integer> orderNumber;

	@FXML
	private TableColumn<Order, OrderType> orderType;

	@FXML
	private TableColumn<Order, String> restName;

	@FXML
	private TableColumn<Order, Float> totalPrice;

	@FXML
	private TableColumn<Order, String> timeOfOrder;

	@FXML
	private TableColumn<Order, String> dateOfOrder;

	@FXML
	private TableColumn<Order, String> orderStatus;

	@FXML
	private TableColumn<Order, String> costumerID;

	@FXML
	private Label labelArrivalTime;

	@FXML
	private TextField setArrivalTimeIsPlaaned;

	@FXML
	private Text userName;

	@FXML
	private ImageView homePage;

	@FXML
	private Button logout;

	ObservableList<Order> list;
	boolean confirmedArrivalTimeFlag = false;
	boolean waitForArrivalTimeFlag = false;
	boolean RegularOrSharedFlag = false;
	boolean wrongArrivalTimeFlag = false;
	private String ArrivalTime;
	private String[] DivededUandP;

	/**
	 * This method meant to get back to supplier page
	 * 
	 * @param event = ActionEvent
	 */
	@FXML
	void backToHome(MouseEvent event) throws IOException {
		start(event, "SupplierScreen", "Supplier page", LoginScreenController.user.getFirstN());
	}

	/**
	 * This method meant to get back to login page and logout the supplier
	 * 
	 * @param event = ActionEvent
	 */
	@FXML
	void logout(ActionEvent event) throws IOException {
		ClientUI.chat.accept(new Message(MessageType.Disconected, LoginScreenController.user.getUserName()));
		start(event, "LoginScreen", "Login", "");
	}

	/**
	 * A method of confirming an order waiting for approval and calculate the data into the reports.
	 * @param event = ActionEvent
	 */
	@FXML
	void confirmOrder(ActionEvent event) throws IOException {
		Order orderToChange = null;
		boolean confirmFlag = true;
		list = table.getSelectionModel().getSelectedItems();
		try {
			orderToChange = list.get(0);
		} catch (IndexOutOfBoundsException e) {
			confirmFlag = false;
		}
		if (confirmFlag) {
			if (!orderToChange.getOrderStatus().equals("Approved")) {
				if (!orderToChange.getUseRefund().equals("0")) {
					orderToChange.setTotalPrice(
							orderToChange.getTotalPrice() - Float.parseFloat(orderToChange.getUseRefund()));
					ClientUI.chat.accept(new Message(MessageType.Use_Refund, orderToChange));
				}
				if (orderToChange.getUseBudget() == 1)
					ClientUI.chat.accept(new Message(MessageType.Use_Budget, orderToChange));
				ClientUI.chat.accept(new Message(MessageType.Order_approved, orderToChange));
				for (int i = 0; i < allOrders.size(); i++) {
					if (allOrders.get(i).equals(orderToChange))
						allOrders.get(i).setOrderStatus("Approved");
				}
				if (LocalDate.now().isAfter(LocalDate.parse(orderToChange.getDateOfOrder()))
						|| LocalDate.now().isEqual(LocalDate.parse(orderToChange.getDateOfOrder()))) {
					Float priceAfterCommision = (float) (orderToChange.getTotalPrice()
							- (orderToChange.getTotalPrice() * 0.1));
					Receipt receiptToAdd = new Receipt(orderToChange.getOrderNum(), orderToChange.getRestName(),
							orderToChange.getTotalPrice(), priceAfterCommision);
					ViewReceiptController.receipts.add(receiptToAdd);
				}
				String str = "The order was successfully confirmed.\n Dear customer, your order number: "
						+ orderToChange.getOrderNum() + "  was successfully received at the restaurant.";
				PopUpMessage.simulationMessage(str);
				String[] dic = orderToChange.getDateOfOrder().split("-");
			String Quarterly;
			Quarterly = checkQuarterly(dic[1]);
			branch = homeBranches.BranchToString(LoginScreenController.user.getHomeBranch());
			// LoginScreenController.user.PrintUser();
			RevenueReport revenuereport = new RevenueReport(orderToChange.getRestName(), branch, dic[1], dic[0],
					Quarterly, 1, orderToChange.getTotalPrice());
			ClientUI.chat.accept(new Message(MessageType.addto_Revenue_report, revenuereport));
			Integer id = orderToChange.getOrderNum();
			ArrayList<OrdersReport> ordersreports = new ArrayList<>();
			ClientUI.chat.accept(new Message(MessageType.get_Dish_type, id));
			for (String s : dishTypesQuentities.keySet()) {
				String dishType = s;
				int quentity = dishTypesQuentities.get(s);
				OrdersReport orderreport = new OrdersReport(dic[1], dic[0], branch, orderToChange.getRestName(),
						dishType, quentity);
				ordersreports.add(orderreport);
			}
			ClientUI.chat.accept(new Message(MessageType.addto_Order_report, ordersreports));
				orderToChange = null;
				table.refresh();
				list = FXCollections.observableArrayList(allOrders);
				table.setItems(list);
			}
		}
	}

	String checkQuarterly(String month) {
		switch (month) {
		case "01":
			return "1";
		case "02":
			return "1";
		case "03":
			return "1";
		case "04":
			return "2";
		case "05":
			return "2";
		case "06":
			return "2";
		case "07":
			return "3";
		case "08":
			return "3";
		case "09":
			return "3";
		case "10":
			return "4";
		case "11":
			return "4";
		case "12":
			return "4";
		default: {
			return null;
		}
		}
	}

	/**
	 * A method of refusing an order waiting for approval.
	 * 
	 * @param event = ActionEvent
	 */

	@FXML
	void refuseOrder(ActionEvent event) throws IOException {
		Order orderToChange = null;
		boolean refuseFlag = true;
		list = table.getSelectionModel().getSelectedItems();
		try {
			orderToChange = list.get(0);
		} catch (IndexOutOfBoundsException e) {
			refuseFlag = false;
		}
		if (refuseFlag) {
			ClientUI.chat.accept(new Message(MessageType.Order_not_approved, orderToChange)); // Change the status of																				// the
			// database to Not approved
			for (int i = 0; i < allOrders.size(); i++) {
				if (allOrders.get(i).equals(orderToChange))
					allOrders.remove(i);
			}
			orderToChange = null;
			list = FXCollections.observableArrayList(allOrders);
			table.setItems(list);
		}
	}

	/**
	 * A method of Sending an order waiting for send.
	 * 
	 * @param event = ActionEvent
	 */
	@FXML
	void SendOrder(ActionEvent event) {
		boolean continueFlag = true;
		boolean sendFlag = true;
		Order orderToChange = null;
		list = table.getSelectionModel().getSelectedItems();
		try {
			orderToChange = list.get(0);
		} catch (IndexOutOfBoundsException e) {
			sendFlag = false;
		}
		if (sendFlag) {
			if (!orderToChange.getOrderStatus().equals("Approved"))
				PopUpMessage.errorMessage("Order must be approved before sended to client");
			else {
				if (LocalDate.now().isAfter(LocalDate.parse(orderToChange.getDateOfOrder()))
						|| LocalDate.now().isEqual(LocalDate.parse(orderToChange.getDateOfOrder()))) {
					for (int i = 0; i < allOrders.size(); i++) {
						if (allOrders.get(i).equals(orderToChange))
							allOrders.remove(i);
					}
					ClientUI.chat.accept(new Message(MessageType.get_Phone_Number, orderToChange));
					StringBuilder str = new StringBuilder();
					str.append("Dear customer, The order was sent successfully. ");
					str.append("The phone number is - " + phoneNumber);
					if (orderToChange.getOrderType().equals("Regular")
							|| orderToChange.getOrderType().equals("Shared")) {
						RegularOrSharedFlag = true;
					}
					if (RegularOrSharedFlag) {
						if (setArrivalTimeIsPlaaned.getText().isEmpty()) {
							waitForArrivalTimeFlag = false;
							PopUpMessage.errorMessage("You have to insert arrival time");

						} else {
							ArrivalTime = setArrivalTimeIsPlaaned.getText();
							try {
								LocalTime checkTime = LocalTime.parse(ArrivalTime);
								if (checkTime.isAfter(LocalTime.now()))
									waitForArrivalTimeFlag = true;
								else
									wrongArrivalTimeFlag = true;
							} catch (DateTimeParseException | NullPointerException e) {
								PopUpMessage.errorMessage("Time must be invalid");
							}
						}
						if (waitForArrivalTimeFlag)
							str.append(". Your order has been sent to you and will arrive at - " + ArrivalTime);
						else {
							if (wrongArrivalTimeFlag)
								PopUpMessage.errorMessage("Unable to enter elapsed time");
							RegularOrSharedFlag = false;
							wrongArrivalTimeFlag = false;
							continueFlag = false;
							str.setLength(0);
							orderToChange = null;
							setArrivalTimeIsPlaaned.clear();
						}
					}
					if (continueFlag) {
						RegularOrSharedFlag = false;
						waitForArrivalTimeFlag = false;
						ClientUI.chat.accept(new Message(MessageType.Order_sended, orderToChange));
						PopUpMessage.simulationMessage(str.toString());
						orderToChange = null;
						list = FXCollections.observableArrayList(allOrders);
						table.setItems(list);
						str.setLength(0);
						setArrivalTimeIsPlaaned.clear();
					}
				} else {
					PopUpMessage.errorMessage("The order must be sent on the requested day.");
				}
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		labelArrivalTime.setVisible(true);
		setArrivalTimeIsPlaaned.setVisible(true);
		orderNumber.setCellValueFactory(new PropertyValueFactory<Order, Integer>("orderNum"));
		orderType.setCellValueFactory(new PropertyValueFactory<Order, OrderType>("orderType"));
		restName.setCellValueFactory(new PropertyValueFactory<Order, String>("restName"));
		totalPrice.setCellValueFactory(new PropertyValueFactory<Order, Float>("totalPrice"));
		timeOfOrder.setCellValueFactory(new PropertyValueFactory<Order, String>("timeOfOrder"));
		dateOfOrder.setCellValueFactory(new PropertyValueFactory<Order, String>("dateOfOrder"));
		orderStatus.setCellValueFactory(new PropertyValueFactory<Order, String>("orderStatus"));
		costumerID.setCellValueFactory(new PropertyValueFactory<Order, String>("costumerId"));
		DivededUandP = ((String) LoginScreenController.user.getRole()).split("-");
		System.out.println(DivededUandP[2]);
		ClientUI.chat.accept(new Message(MessageType.get_orders_to_approve, DivededUandP[2]));
		list = FXCollections.observableArrayList(allOrders);
		table.setItems(list);
	}

	@Override
	public void display(String string) {
		userName.setText(LoginScreenController.user.getFirstN());
	}
}