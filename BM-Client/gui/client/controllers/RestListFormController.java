package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import Entities.Dish;
import Entities.Message;
import Entities.MessageType;
import Entities.Restaurant;
import Entities.SingletonOrder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import main.ClientUI;


/**This class shows the customer list of restaurant in specific city 
 * @author Adi 
 *@author Talia
 * 
 *
 */
public class RestListFormController extends AbstractController implements Initializable {

	@FXML
	private ImageView BackImage;

	@FXML
	private Button backBtn;

	@FXML
	private Text cityName;

    @FXML
    private TableColumn<Restaurant, String> colAdd;

    @FXML
    private TableColumn<Restaurant, String> colOpen;

    @FXML
    private TableColumn<Restaurant, String> colRes;

    @FXML
    private Button nextbtn;
    
    @FXML
    private ImageView homePage;

    @FXML
    private Button logout;
    

    @FXML
    private Text userName;
    
    @FXML
    private Label notify;

    @FXML
    private TableView<Restaurant> table;
    
    public static ArrayList<Restaurant> restaurants=new ArrayList<>();
    
    public static ArrayList<Dish> dishes=new ArrayList<>();

    
    public static Restaurant chosenRst;
    
    
	/** This method meant to get back to costumer page
	 * @param event				pressing the "home" image 
	 * @throws IOException
	 */
    @FXML
    void backToHome(MouseEvent event) throws IOException {
    	if(SingletonOrder.getInstance()!=null)
    	{
    		SingletonOrder.getInstance().myOrder.clear();
    	}
    	start(event, "CustomerScreen", "CustomerScreen",LoginScreenController.user.getFirstN());
    }
    
    
	/** This method meant to get back to login page and logout the customer
	 * @param event				pressing the "logout" button 
	 * @throws IOException
	 */

    @FXML
    void logout(ActionEvent event) throws IOException {
    	logoutForCustomer();
    	ClientUI.chat.accept(new Message(MessageType.Disconected,LoginScreenController.user.getUserName()));
		start(event, "LoginScreen", "Login","");
    }
    

	/** This method meant to get back to choosing city
	 * @param event				pressing the "back" button 
	 * @throws IOException
	 */

    @FXML
    void backToCity(ActionEvent event) throws IOException {
		start(event,"ChooseRestaurant","Choose city",LoginScreenController.user.getFirstN());
    }

    
    /**This method proceed to the menu of the restaurant
     * @param event		pressing the next button
     * @throws IOException
     */
    @FXML
    void proceedToOrder(ActionEvent event) throws IOException {
		if(table.getSelectionModel().getSelectedItem()!=null)
		{
			String supplier=table.getSelectionModel().getSelectedItem().getSupplierName();
			String address=table.getSelectionModel().getSelectedItem().getAddress();
			ClientUI.chat.accept(new Message(MessageType.get_Dishes,table.getSelectionModel().getSelectedItem().getRestCode()));
			for(Restaurant r:restaurants)
			{
				if(supplier.equals(r.getSupplierName())&& address.equals(r.getAddress()))
					chosenRst=r;	
			}
			start(event,"MenuScreen","Restaurant's menu","");
		}
		else
		{
			notify.setText("In order to proceed please choose restaurant");
		}
    }

	
	/**This method is initializes the table with the restaurant in the wanted city  
	 *
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ClientUI.chat.accept(new Message(MessageType.show_Restaurants,ChooseRestController.cityName));
		ObservableList<Restaurant> observableList = FXCollections.observableArrayList(restaurants);
		colAdd.setCellValueFactory(new PropertyValueFactory<>("address"));
		colOpen.setCellValueFactory(new PropertyValueFactory<>("openning"));
		colRes.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
		table.setItems(observableList);
	}

	
	/**
	 * @param city
	 */
	public void display(String city) {
		cityName.setText(city);
		userName.setText(LoginScreenController.user.getFirstN());
	}
}