package com.smartFarmApp.view;

import javax.swing.JOptionPane;

import com.smartFarmApp.dashboard.Main;
import com.smartFarmApp.dashboard.items.*;


import javafx.animation.PathTransition;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import javafx.util.Duration;
import javafx.scene.text.Text;

/*
 * Controller class to handle user interaction with
 * SmartFarmDashboard
 */

public class SmartFarmAppDashboardController {


    private static SmartFarmAppDashboardController INSTANCE;
    // Model

    public static SmartFarmAppDashboardController getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new SmartFarmAppDashboardController();
        }
        return INSTANCE;
    }

    // Model
    private ArrayList<Item> farmItems;
    /* TODO:
     * @FXML annotations
     *  initialize() method
     */

    /*
     * FXML annotations
     */
    //Left side of Dashboard (Actions/Items)
    @FXML
    private SplitPane ControlsSplitPane;
    @FXML
    private AnchorPane ItemsAnchorPane;
    @FXML
    private AnchorPane ActionsAnchorPane;
    @FXML
    private Label ItemsLabel;
    @FXML
    private TreeView<Item> ItemsTreeView;
    @FXML
    private GridPane SelectedItemProperties;
    @FXML
    private Label purchasePrice;
    @FXML
    private Label marketPrice;
    // Selected Item Properties
    @FXML
    private TextField SelectedName;
    @FXML
    private TextField SelectedPosX;
    @FXML
    private TextField SelectedPosY;
    @FXML
    private TextField SelectedLength;
    @FXML
    private TextField SelectedWidth;
    @FXML
    private TextField SelectedHeight;
    @FXML
    private TextField SelectedPrice;

    // Drone Actions
    @FXML
    private Label DroneActionsLabel;
    @FXML
    private Button VisitItemAction;
    @FXML
    private Button ScanFarmAction;
    @FXML
    private Button AddDrone;


    //Right Side (Farm View)
    @FXML
    private AnchorPane FarmViewVBox;
    @FXML
    private ImageView drone;
    @FXML
    private ImageView crops;
    @FXML
    private ImageView barn;

    /*
     * End of FXML annotations
     */

    //Reference to Main
    private Main main;

    //Drone visits the item that is selected in the treeview then returns to it's location
    //Before visiting the item
    @FXML
    private void handleVisitItemAction(ActionEvent event) {

        DroneAnimation droneAnim = new DroneAnimation(drone);
	
		droneAnim.visitItem(getSelectedItem().getPosX()-20, getSelectedItem().getPosY() - 20);
    }

    @FXML
    private void handleScanFarmAction(ActionEvent event) {

        DroneAnimation droneAnim = new DroneAnimation(drone);

		droneAnim.scanFarm();
        
        //for testing, would be nice to disable button and change it's text to "scanning" while it is running
        System.out.println("Current state: "+ scantransition.getStatus());
        //ScanFarmAction.setText("Scanning...");
        //ScanFarmAction.setText("Scan Farm");
    }

    //Adds the drone and the command center
    @FXML
    private void addDrone(ActionEvent event) {
        drone.setVisible(true);
        int index = getSelectedItemIndex();
        farmItems.add(new ItemContainer("Command Center", 0, 5,5,130,130,130, new ArrayList<Item>(){
            {
                add(new ItemLeaf("Drone", 4, 20, 20, 50, 50, 50));
            }
        }));
        updateFarmItemsTree();
        MultipleSelectionModel msm = ItemsTreeView.getSelectionModel();
        msm.select(index);
    }



    //Add Item Leaf
    @FXML
    private void addNewFarmItemLeaf(ActionEvent event) {
        addNewFarmItem(new ItemLeaf("New_Item",0,0,0,0,0,0));
    }

    //Add Item Container
    @FXML
    private void addNewFarmItemContainer(ActionEvent event) {
        addNewFarmItem(new ItemContainer("New_ItemContainer",0,0,0,0,0,0, new ArrayList<Item>()));
    }

    //Add new item/itemcontainer to the farm
    private void addNewFarmItem(Item newItem) {
        int index = getSelectedItemIndex();
        Item selectedItem = getSelectedItem();
        if (selectedItem != null && selectedItem.getChildren() != null) {
            selectedItem.addChild(newItem);
        } else {
            farmItems.add(newItem);
        }
        updateFarmItemsTree();
        MultipleSelectionModel msm = ItemsTreeView.getSelectionModel();
        msm.select(index);
    }

    //Deletes selected item from farm
    @FXML
    private void deleteFarmItem(ActionEvent event) {
        Item itemToRemove = getSelectedItem();
        if (itemToRemove == null || String.valueOf(itemToRemove) == "Farm") return;
        TreeItem<Item> item = ItemsTreeView.getSelectionModel().getSelectedItem();
        TreeItem<Item> itemParent = item.getParent();

        if (String.valueOf(itemParent.getValue()) == "Farm") {
            farmItems.remove(itemToRemove);
        } else {
            itemParent.getValue().removeChild(itemToRemove);
        }

        updateFarmItemsTree();
    }

    //Saves the values for the current selected item
    //Checks to make sure inputs are valid for item
    @FXML
    private void saveSelectedItemChanges(ActionEvent event) {
        Item selectedItem = getSelectedItem();
        int index = getSelectedItemIndex();
        selectedItem.setName(SelectedName.getText());
        selectedItem.setPosition(Integer.parseInt(SelectedPosX.getText()), Integer.parseInt(SelectedPosY.getText()));
        selectedItem.setDimensions(Integer.parseInt(SelectedLength.getText()),
                Integer.parseInt(SelectedWidth.getText()),
                Integer.parseInt(SelectedHeight.getText()));
        selectedItem.setPrice(Double.parseDouble(SelectedPrice.getText()));

        //Error checking to make sure item/container will not extend outside of the farm's x and y boundaries
        if (selectedItem.getPosX() + selectedItem.getLength() >= 600)
        {
            JOptionPane.showMessageDialog(null, "Entered Values Not Saved\nBeginning X Value and Length of Item extends outside of farm area for Farm Item: " + selectedItem.getName() +
                            "\nPlease add a valid X Value and Length that add up to be less than 600",
                    "InfoBox: X Coordinate Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selectedItem.getPosY() + selectedItem.getWidth() >= 800)
        {
            JOptionPane.showMessageDialog(null, "Entered Values Not Saved\nBeginning Y Value and Width of Item extends outside of farm area for Farm Item: " + selectedItem.getName() +
                            "\nPlease add a valid Y Value and Width that add up to be less than 800",
                    "InfoBox: X Coordinate Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ItemsTreeView.refresh();
        updateFarmItemsTree();
        MultipleSelectionModel msm = ItemsTreeView.getSelectionModel();
        msm.select(index);
    }

    //Draws an itemLeaf
    private void drawItem(Item item) {
        Rectangle rect = new Rectangle(item.getPosX(), item.getPosY(), item.getLength(), item.getWidth());
        rect.setAccessibleText(item.getName());
        rect.setStroke(Color.RED);
        rect.setStrokeWidth(2);
        rect.setFill(Color.TRANSPARENT);
        FarmViewVBox.getChildren().add(rect);
    }

    private void getMarketPrice(){
        Item selectedItem = getSelectedItem();

        double itemMarketPrice = selectedItem.getMarketPrice();
        System.out.println(itemMarketPrice);

    }

    //Draws an ItemContainer
    private void drawItemContainer(Item itemContainer) {
        Rectangle rect = new Rectangle(itemContainer.getPosX(), itemContainer.getPosY(), itemContainer.getLength(), itemContainer.getWidth());
        rect.setAccessibleText(itemContainer.getName());
        rect.setStroke(Color.GREEN);
        rect.setStrokeWidth(5);
        rect.setFill(Color.TRANSPARENT);
        FarmViewVBox.getChildren().add(rect);
    }

    // Redraws the Farm Items Tree according to the farmItems ArrayList
    // Also redraws the visual items/containers in the farm
    private void updateFarmItemsTree() {
        ItemsTreeView.getRoot().getChildren().clear();
        FarmViewVBox.getChildren().clear();
        farmItems.forEach(item -> {
            addChildrenToItemsTree(item, ItemsTreeView.getRoot());
            drawTreeItems(item, ItemsTreeView.getRoot());
        });
    }

    // If item is Leaf, add self. If item has children, add self and then recurse on children.
    void addChildrenToItemsTree(Item item, TreeItem<Item> parent) {
        TreeItem<Item> newTreeItem = new TreeItem<Item>(item);
        parent.getChildren().add(newTreeItem);
        while (item.hasNext()) {
            addChildrenToItemsTree(item.next(), newTreeItem);
        }
    }

    //Loops through tree and redraws all containers/leafs
    void drawTreeItems(Item item, TreeItem<Item> parent) {
        TreeItem<Item> newTreeItem = new TreeItem<Item>(item);
        Text text = new Text(item.getPosX(), item.getPosY() - 5, item.getName());
        text.setAccessibleText(item.getName());
        FarmViewVBox.getChildren().add(text);
        if (item != null && item.getChildren() != null)
        {
            drawItemContainer(item);
        } else {
            if (item.getName().equals("Drone")) {
                drone.setX(item.getPosX());
                drone.setY(item.getPosY());
                FarmViewVBox.getChildren().add(drone);
            }
            else {drawItem(item);}
        }
        while (item.hasNext()) {
            drawTreeItems(item.next(), newTreeItem);
        }
    }

    @FXML
    public void initialize() {
        // Initialize farmItems
        farmItems = new ArrayList<Item>();
        // Establish a Root Item
        ItemLeaf farm = new ItemLeaf("Farm", 0, 0,0,0,0,0);
        TreeItem<Item> root = new TreeItem<Item>(farm);
        root.setExpanded(true);
        ItemsTreeView.setRoot(root);

        // Add event listeners
        ItemsTreeView.getSelectionModel().selectedItemProperty().addListener((ChangeListener)
                (observable, oldValue, newValue) -> {
                    Item selectedItem = getSelectedItem();
                    if (selectedItem == null) {
                        ItemsTreeView.getSelectionModel().select(ItemsTreeView.getRoot());
                        selectedItem = getSelectedItem();
                    };
                    SelectedName.setText(selectedItem.getName());
                    SelectedPosX.setText(String.valueOf(selectedItem.getPosX()));
                    SelectedPosY.setText(String.valueOf(selectedItem.getPosY()));
                    SelectedLength.setText(String.valueOf(selectedItem.getLength()));
                    SelectedWidth.setText(String.valueOf(selectedItem.getWidth()));
                    SelectedHeight.setText(String.valueOf(selectedItem.getHeight()));
                    SelectedPrice.setText(String.valueOf(selectedItem.getPrice()));
                    if(selectedItem.getName() != "Farm"){
                        String purchasePriceText = String.valueOf(selectedItem.accept(new Price()));
                        String marketPriceText = String.valueOf(selectedItem.accept(new MarketPrice()));
                        System.out.println("Market Price = " + marketPriceText + ", Price = " + purchasePriceText);
                        purchasePrice.setText(purchasePriceText);
                        marketPrice.setText(marketPriceText);
                    }
                });
    }

    //Gets the index of the selected item in treeview
    int getSelectedItemIndex() {
        int selectedTreeItemIndex = ItemsTreeView.getSelectionModel().getSelectedIndex();
        return selectedTreeItemIndex;
    }

    //Gets the value of the selected item in treeview
    Item getSelectedItem() {
        TreeItem<Item> selectedTreeItem = (TreeItem<Item>) ItemsTreeView.getSelectionModel().getSelectedItem();
        if (selectedTreeItem == null) { return null;}
        return selectedTreeItem.getValue();
    }

}
