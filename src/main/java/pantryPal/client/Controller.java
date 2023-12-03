package pantryPal.client;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import pantryPal.client.UserAccount.AccountManager;
import pantryPal.client.UserAccount.User;
import pantryPal.client.View.HomePageAppFrame;
import pantryPal.client.View.HomePageHeader;
import pantryPal.client.View.InputAppFrame;
import pantryPal.client.View.LoginPageAppFrame;
import pantryPal.client.View.RecipeDisplayAppFrame;
import pantryPal.client.View.RecButtons;
import pantryPal.client.View.RecipeDisplay;
import pantryPal.client.View.RecipeTitle;
import pantryPal.client.View.UI;
import com.sun.net.httpserver.*;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.type;

import java.io.*;
import java.net.*;

public class Controller {

    // private Input input = new Input();
    // private RecipeCreator rc = new RecipeCreator();
    private InputAppFrame inputFrame;
    private RecipeParser rp = new RecipeParser();
    private LoginPageAppFrame lp;
    private UI ui;
    private HomePageAppFrame hp;
    private RecipeDisplayAppFrame rd;
    private RecipeTitle rt = new RecipeTitle("", "");
    private Model model;
    private String filterState = "All Recipes";
    private String sortState = "Default";

    private String name = "";

    public Controller(String name, UI ui, Model model) {
        this.name = name;
        this.model = model;
        this.ui = ui;
        this.inputFrame = ui.getInputPage();
        this.hp = ui.getHomePage();
        this.rd = ui.getDisplayPage(); 
        this.lp = ui.getLoginPage();     
        
        
        this.inputFrame.setStartButtonAction(this::handleStartButton);
        this.inputFrame.setStopButtonAction(event -> {
            try {
                handleStopButton(event);
            } catch (InterruptedException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.inputFrame.setBackButtonAction(this::handleBackButton);
        this.rd.setBackButtonAction2(this::handleBackButton2);
        this.rd.setDeleteButtonAction(this::handleDeleteButton);
        this.hp.setCreateButtonAction(this::handleCreateButton);
        this.hp.setFilterButtonAction(this::handleFilterButton);
        this.hp.setSortButtonAction(this::handleSortButton);
        this.rd.setSaveButtonAction(this::handleSaveButton);
        this.rd.setEditButtonAction(this::handleEditButton);
        this.rt.setViewButtonAction(this::handleViewButton);
        this.rd.setShareButtonAction(event -> {
            try {
                handleShareButton(event);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.rd.setRegenerateButtonAction(event -> {
            try {
                handleRegenerateButton(event);
            } catch (InterruptedException | IOException | URISyntaxException  e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.rd.setShareButtonAction(event -> {
            try {
                handleShareButton(event);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.lp.setLoginButtonAction(this::handleLoginButton);
        this.lp.setCreateAccButtonAction(this::handleCreateAccButton);
        this.inputFrame.setLogoutButtonAction(this::handleLogoutButton2);
        this.rd.setLogoutButtonAction(this::handleLogoutButton);
        this.hp.setLogoutButtonAction(this::handleLogoutButton);
        
    }

    public void handleCreateButton(ActionEvent event) {
        ui.getRoot().setCenter(inputFrame);
        ui.getRoot().setTop(inputFrame.getReturnHeader());
        ui.getRoot().setBottom(null);
    }

    // TODO: sort shouldnt be called serverside
    public void handleSortButton(ActionEvent event) {
        
        sortState = hp.getHomePageFooter().getSortButton().getValue();
        loadRecipes();
        filter();

        
        // else { // filter and reverse/date created?
        //     hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true);
        //     try {
        //         ArrayList<String[]> recipes = RecipeManager.sortRecipes(selectedMealType, filterState);
        //         for(int i = 0; i < recipes.size(); i++){
        //             String stringID = recipes.get(i)[0];
        //             String title = recipes.get(i)[1];
        //             String ingredients = recipes.get(i)[2];
        //             String steps = recipes.get(i)[3];
        //             String mealType = recipes.get(i)[4];
        //             String imageURL = recipes.get(i)[5];
        //             RecipeDisplay recipeDisplay = new RecipeDisplay(stringID, title, ingredients, steps, imageURL, mealType);
        //             RecipeDisplayAppFrame rec = new RecipeDisplayAppFrame(recipeDisplay);
        //             RecipeTitle recipeTitle = new RecipeTitle(stringID, title, rec, mealType);
        //             rec.setID(recipeTitle.getID());
        //             recipeTitle.getViewButton().setOnAction(e1->{
        //                     ui.getRoot().setCenter(recipeTitle.getRecipeDetail()); 
        //                     ui.getRoot().setTop(recipeTitle.getRecipeDetail().getRecipeDisplayHeader());
        //                     ui.getRoot().setBottom(null);
        //                     //this.rd = rec;
        //                     rec.setEditButtonAction(this::handleEditButton);
        //                     rec.setSaveButtonAction(this::handleSaveButton);
        //                     rec.setDeleteButtonAction(this::handleDeleteButton);
        //             });
        //             hp.getRecipeList().getChildren().add(recipeTitle);
        //             recipeTitle.getRecipeDetail().setBackButtonAction2(this::handleBackButton2);
        //             recipeTitle.getRecipeDetail().setLogoutButtonAction(this::handleLogoutButton);
        //         }
            // } catch (Exception e) {
            //     // TODO: handle exception
            //     System.out.println("SORTING ERROR");
            // }
        
        System.out.println("done sorting");
    }

    public void handleShareButton(ActionEvent event) throws IOException {
        this.rd.getRecipe().getShareButton().setStyle("-fx-background-color: #5DBB63; -fx-border-width: 0;");
        PauseTransition pause = new PauseTransition(
            Duration.seconds(1)
        );
        pause.setOnFinished(e2 -> {
            this.rd.getRecipe().getShareButton().setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0;");
        });
        pause.play();
        System.out.println("sharing recipe");
        model.performRequest("GET", rd.getMealType(), rd.getID(), rd.getStringTitle(), rd.getStringIngredients(), rd.getStringSteps(), rd.getImage(), this.name);                 
        /* String response = model.performRequest("GET", rd.getMealType(), rd.getID(), rd.getStringTitle(), rd.getStringIngredients(), rd.getStringSteps(), rd.getImage(), this.name);                 
        if (response.equals("Account not found")) {
            System.out.println("Recipe not found");
        }
        System.out.println(response);*/
    }
    
    
    public void handleFilterButton(ActionEvent event) {
        
        filterState = hp.getHomePageFooter().getFilterButton().getValue();
        loadRecipes();
        filter();
        
    }

    public void handleStartButton(ActionEvent event) {
        try {
            RecButtons rb = inputFrame.getRecButtons();
            rb.setRecipeText("Recording");
            // perform request 
            
            model.performRequest("start", "Whisper");
            
            // input.captureAudio();
            inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStartButton());
            inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStopButton());
        }
        catch (ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        }
    }

    public void handleStopButton(ActionEvent event) throws InterruptedException, IOException {

        // String promptType = input.getPromptType();
        inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStopButton());
        inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStartButton());

        String response = model.performRequest("stop", "Whisper");

        if(response.equals("Breakfast") || response.equals("Lunch") || response.equals("Dinner")){
            
            inputFrame.getRecButtons().setRecipeText("Please input Ingredients.\n\nMeal Type: " + response);
            inputFrame.setMealType(response);
        }
        else if (response.equals("valid")){    
            inputFrame.getRecButtons().setRecipeText("Recipe Displayed");
            // input.setPromptType("MealType");
            String prompt = generateRecipe();
            model.performRequest(prompt, "ChatGPT");
            RecipeDisplay rec = new RecipeDisplay();

            try {
                rp.parse(); 
                rec.setID(null);
                rec.setTitle(rp.getTitle());
                rec.setIngreds(rp.getStringIngredients());
                rec.setSteps(rp.getStringSteps());
                rec.setMealType(inputFrame.getMealType());

                // File oldFile = new File("generated_img/temp.jpg");
                // oldFile.delete();
                String imagePrompt = "Display the dish: " + rp.getTitle() + ", a dish with the ingredients: " + rp.getStringIngredients() + ", like it is a dish in a Recipe Book";

                String imgURL = model.performRequest(imagePrompt, "DallE");

                rec.setImage(imgURL);

                // System.out.println(rec.getIngredients().getText());
                // System.out.println(rec.getSteps().getText());
                // System.out.println("SDUHFIOSDHFIOSHDOFHSDIOFHSDIOFSIDHFOSDIFHSODi");
                RecipeDisplayAppFrame displayRec = new RecipeDisplayAppFrame(rec);
                displayRec.setBackButtonAction2(this::handleBackButton2);
                displayRec.setLogoutButtonAction(this::handleLogoutButton);
                displayRec.setDeleteButtonAction(this::handleDeleteButton);
                displayRec.setSaveButtonAction(this::handleSaveButton);
                displayRec.setEditButtonAction(this::handleEditButton);
                displayRec.setRegenerateButtonAction(ev -> {
                    try {
                        handleRegenerateButton(ev);
                    } catch (InterruptedException | IOException | URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                displayRec.setShareButtonAction(ev -> {
                    try {
                        handleShareButton(ev);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                this.rd = displayRec;
                            
                ui.setDisplayPage(displayRec);
                ui.getRoot().setCenter(displayRec);
                ui.getRoot().setTop(displayRec.getRecipeDisplayHeader());
                // recipeText.setText(text);
                // br.close();
            } catch(Exception err){
                err.printStackTrace();
            }
        }
        else{
            
            inputFrame.getRecButtons().setRecipeText("Invalid Input. Please say a proper meal type.\n\nTranscription: " + response);
        }
        
        
    }

    private void handleBackButton(ActionEvent event){
        
        ui.returnHomePage();   
        resetInput();

    }

    private void handleBackButton2(ActionEvent event){
        ui.returnHomePage();   
        inputFrame.getRecButtons().setRecipeText("Select Meal Type: Breakfast, Lunch, or Dinner");    
        if(this.rd.getEditable()){
            RecipeDisplayAppFrame r = this.rd;
            TextArea ingredients = r.getIngredients();
            Button editButton = r.getEditButton();
            TextArea steps = r.getSteps();
            ingredients.setEditable(false);
            steps.setEditable(false);
            ImageView editImage = new ImageView(new Image("file:graphics/e2.png"));
            editImage.setPreserveRatio(true);
            editImage.setFitHeight(25);
            editImage.setFitWidth(45);
            editButton.setGraphic(editImage);
            r.setEditable(false);
            reload();
            ui.returnHomePage();
        }
    }

    private void handleEditButton(ActionEvent event) {
        boolean editable = rd.getEditable();
        TextArea ingredients = rd.getIngredients();
        Button editButton = rd.getEditButton();
        TextArea steps = rd.getSteps();
        if (!editable) {
                ingredients.setEditable(true);
                steps.setEditable(true);
                ImageView editImage = new ImageView(new Image("file:graphics/st2.png"));
                editImage.setPreserveRatio(true);
                editImage.setFitHeight(25);
                editImage.setFitWidth(45);
                editButton.setGraphic(editImage);
                rd.setEditable(true);
            }
        else {
            ingredients.setEditable(false);
            steps.setEditable(false);
            ImageView editImage = new ImageView(new Image("file:graphics/e2.png"));
            editImage.setPreserveRatio(true);
            editImage.setFitHeight(25);
            editImage.setFitWidth(45);
            editButton.setGraphic(editImage);
            rd.setEditable(false);
        }
    }

    public void handleSaveButton(ActionEvent event)  {
        try {
            //TODO make sure all windows library added to main
            rd.getIngredients().setEditable(false);
            rd.getSteps();
            if (rd.getID() == null) { // if does not exist in MongoDB 
                // System.out.println("HANDLE SAVE BUTTON (CONTROLLER)");
                String stringID = rd.getID();
                String title = rd.getTitle().getText();
                String ingredients = rd.getIngredients().getText();
                String steps = rd.getSteps().getText();
                String imgURL = rd.getImage();
                String mealType = rd.getMealType(); 
                System.out.println("kekekekekkekekekek" + mealType);
                model.performRequest("PUT", mealType, stringID, title, ingredients, steps, imgURL, this.name);
                // TODO: Add mealType Tag to recipe display
                RecipeDisplay recipeDisplay = new RecipeDisplay(stringID, title, ingredients, steps, imgURL, mealType);
                RecipeDisplayAppFrame rec = new RecipeDisplayAppFrame(recipeDisplay);
                RecipeTitle recipeDis = new RecipeTitle(stringID, title, rec, mealType);
                rd.setID(RecipeManager.getStringID()); // TODO CHANGE?? 

            

                // File oldFile = new File("generated_img/temp.jpg");
                // File newFile = new File("generated_img/" + title.replace(" ","") + ".jpg");
                // boolean success = oldFile.renameTo(newFile);

                recipeDis.setViewButtonAction(this::handleViewButton);
                recipeDis.getRecipeDetail().setBackButtonAction2(this::handleBackButton2);
                recipeDis.getRecipeDetail().setLogoutButtonAction(this::handleLogoutButton);
                this.rt = recipeDis;
                hp.getRecipeList().getChildren().add(recipeDis);
                reload();


            }
            else {
                model.performRequest("PUT", rd.getMealType(), rd.getID(), rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText(), rd.getRecipe().getImage(), this.name);                
            }
            this.rd.getRecipe().getSaveButton().setStyle("-fx-background-color: #5DBB63; -fx-border-width: 0;");
            PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
            );
            pause.setOnFinished(e2 -> {
                this.rd.getRecipe().getSaveButton().setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0;");
            });
            pause.play();
        }
        catch (ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        }
    }

    private void handleViewButton(ActionEvent event){
        try {
            String imagePrompt = "Display the dish: " + rt.getTitle() + ", a dish with the ingredients: " + rt.getRecipeDetail().getIngredients() + ", like it is a dish in a Recipe Book";

            String imgURL = model.performRequest(imagePrompt, "DallE");
            System.out.println(imgURL);
            this.rt.getRecipeDetail().getRecipe().setImage(imgURL);
            ui.getRoot().setCenter(this.rt.getRecipeDetail()); 
            ui.getRoot().setTop(this.rt.getRecipeDetail().getRecipeDisplayHeader());
            ui.getRoot().setBottom(null);
        } catch (ConnectException err){
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        }

    }

    private void handleDeleteButton(ActionEvent event) {
        
        try {
            String stringID = rd.getID();
            model.performRequest("DELETE", null, stringID, null , null, null, null, this.name);
            reload();
            ui.returnHomePage();
        }
        catch (ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        } 
    }

    private void handleRegenerateButton(ActionEvent event) throws IOException, InterruptedException, URISyntaxException { 

        // input.setPromptType("MealType");
        RecipeDisplay rec = new RecipeDisplay();
        String prompt = generateRecipe();
        model.performRequest(prompt, "ChatGPT");
        try {
            rp.parse(); 
            rec.setID(null);
            rec.setTitle(rp.getTitle());
            rec.setIngreds(rp.getStringIngredients());
            rec.setSteps(rp.getStringSteps());

            // File oldFile = new File("generated_img/temp.jpg");
            // oldFile.delete();
            String imagePrompt = "Display the dish: " + rp.getTitle() + ", a dish with the ingredients: " + rp.getStringIngredients() + ", like it is a dish in a Recipe Book";

            String imgURL = model.performRequest(imagePrompt, "DallE");

            rec.setImage(imgURL);
            System.out.println(rec.getIngredients().getText());
            System.out.println(rec.getSteps().getText());
            System.out.println("SDUHFIOSDHFIOSHDOFHSDIOFHSDIOFSIDHFOSDIFHSODi");
            RecipeDisplayAppFrame displayRec = new RecipeDisplayAppFrame(rec);
            displayRec.setBackButtonAction2(this::handleBackButton2);
            displayRec.setLogoutButtonAction(this::handleLogoutButton);
            displayRec.setDeleteButtonAction(this::handleDeleteButton);
            displayRec.setSaveButtonAction(this::handleSaveButton);
            displayRec.setShareButtonAction(event1 -> {
                try {
                    handleShareButton(event1);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

            
            displayRec.setEditButtonAction(this::handleEditButton);
            displayRec.setRegenerateButtonAction(ev -> {
                try {
                    handleRegenerateButton(ev);
                } catch (InterruptedException | IOException | URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            this.rd = displayRec;
                        
            ui.setDisplayPage(displayRec);
            ui.getRoot().setCenter(displayRec);
            ui.getRoot().setTop(displayRec.getRecipeDisplayHeader());
            ui.getRoot().setBottom(null);

        } catch(IOException err){
            err.printStackTrace();
        } 
        this.rd.getRecipe().getRegenerateButton().setStyle("-fx-background-color: #5DBB63; -fx-border-width: 0;");
        PauseTransition pause = new PauseTransition(
            Duration.seconds(1)
        );
        pause.setOnFinished(e2 -> {
            this.rd.getRecipe().getRegenerateButton().setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0;");
        });
        pause.play();
    }

    private void handleLoginButton(ActionEvent event){
        if(lp.getUsername().length() == 0 || lp.getPassword().length() == 0) {
                lp.setMessage("Username and/or password is empty");
                return;
        }

        try {
            String response = model.performRequest("GET", lp.getUsername(), lp.getPassword());
            
            if (response.equals("Account not found")) {
                lp.setMessage(response);
            }
            else if (response.equals(lp.getPassword())) {
                this.name = lp.getUsername();
                if(lp.getAuto().isSelected()){
                    File file = new File("src/main/resources/autologin.txt");
                    file.createNewFile();
                    BufferedWriter br = new BufferedWriter(new FileWriter(file));
                    br.write(lp.getUsername());
                    br.write("\n");
                    br.close();
                }
                else{
                    boolean success = new File("src/main/resources/autologin.txt").delete();
                }
                loadRecipes();
                
                ui.returnHomePage(); 
            }

            else {
                System.out.println(response);
                lp.setMessage("Incorrect password");
            }
        }catch (ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server Error", ButtonType.OK);
            a.showAndWait();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleCreateAccButton(ActionEvent event) {

        try {
            if(lp.getUsername().length() == 0 || lp.getPassword().length() == 0) {
                lp.setMessage("Username and/or password is empty");
                return;
            }
            String response = model.performRequest("PUT", lp.getUsername(), lp.getPassword()); 
            if(response == null) {
                lp.setMessage("Invalid account details");
            }
            else if(!response.equals("Error handling PUT request")) { //TODO: throws error because response can be Null
                this.name = lp.getUsername();
                if(lp.getAuto().isSelected()){
                    File file = new File("src/main/resources/autologin.txt");
                    file.createNewFile();
                    BufferedWriter br = new BufferedWriter(new FileWriter(file));
                    br.write(lp.getUsername());
                    br.write("\n");
                    br.close();
                }
                else{
                    boolean success = (new File("src/main/resources/autologin.txt")).delete();
                }
                loadRecipes();
                ui.returnHomePage(); 
            } else {
                // TODO: display account creation error
                System.out.println("ACCOUNT CREATION FAILED");
            }

        } catch (ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private void handleLogoutButton(ActionEvent event){
        System.out.println("TESTTESTSTESTSETSETSET");
        ui.setLoginPage();
    }

    private void handleLogoutButton2(ActionEvent event){
        System.out.println("from i");
        ui.setLoginPage();
        resetInput();
    }

    public void reload(){
        hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true);  
        loadRecipes();
    }

    private void resetInput(){
        try {
            String response = model.performRequest("Reset", "Whisper");
            
            inputFrame.getRecButtons().setRecipeText("Select Meal Type: Breakfast, Lunch, or Dinner");  
            
            if (inputFrame.getRecButtons().getButtonBox().getChildren().contains(inputFrame.getRecButtons().getStopButton())){
                inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStopButton());
                inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStartButton());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadRecipes(){
        hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true); 
        // ArrayList<String[]> recipes = RecipeManager.loadRecipes(this.name);
        try {
            String response = model.performRequest(this.name);
            JSONArray recipes = new JSONArray(response);
            
            for(int i = 0; i < recipes.length(); i++){
                String recipeString = recipes.getString(i);
                JSONObject recipe = new JSONObject(recipeString);
                
                String stringID = recipe.getJSONObject("_id").getString("$oid");
                String title = recipe.getString("title");
                String ingredients = recipe.getString("ingredients");
                String steps = recipe.getString("steps");
                String mealType = recipe.getString("mealType");
                String imgURL = recipe.getString("imageURL");
                
                RecipeDisplay recipeDisplay = new RecipeDisplay(stringID, title, ingredients, steps, imgURL, mealType);
                RecipeDisplayAppFrame rec = new RecipeDisplayAppFrame(recipeDisplay);
                RecipeTitle recipeTitle = new RecipeTitle(stringID, title, rec, mealType);
                rec.setID(recipeTitle.getID());
                recipeTitle.getViewButton().setOnAction(e1->{
                    
                    try {

                        String imagePrompt = "Display the dish: " + recipeTitle.getTitle() + ", a dish with the ingredients: " + recipeTitle.getRecipeDetail().getIngredients() + ", like it is a dish in a Recipe Book";

                        String img = model.performRequest(imagePrompt, "DallE");

                        recipeTitle.getRecipeDetail().getRecipe().setImage(img);
                        ui.getRoot().setCenter(recipeTitle.getRecipeDetail()); 
                        ui.getRoot().setTop(recipeTitle.getRecipeDetail().getRecipeDisplayHeader());
                        ui.getRoot().setBottom(null);

                        this.rd = rec;

                        rec.setEditButtonAction(this::handleEditButton);
                        rec.setSaveButtonAction(this::handleSaveButton);
                        rec.setDeleteButtonAction(this::handleDeleteButton);
                        rec.setShareButtonAction(event -> {
                            try {
                                handleShareButton(event);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });
                    }
                    catch (ConnectException err){
                        Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
                        a.showAndWait();
                    }
                });
                
                hp.getRecipeList().getChildren().add(recipeTitle);
                recipeTitle.getRecipeDetail().setBackButtonAction2(this::handleBackButton2);
                recipeTitle.getRecipeDetail().setLogoutButtonAction(this::handleLogoutButton);
            }
        }
        catch(ConnectException err) {
            Alert a = new Alert(AlertType.ERROR, "Server is Offline", ButtonType.OK);
            a.showAndWait();
        }
        sort();
    }

    public String[] readPrompt() throws IOException {
        FileReader fr
        = new FileReader("prompt.txt"); // PLACEHOLDER NAME
        BufferedReader br = new BufferedReader(fr);
        String mealType = br.readLine();
        String prompt = br.readLine();
        String [] info = {prompt,mealType};
        br.close();
        return info;
    }

    public String generateRecipe() throws IOException, InterruptedException {
        String[] info = readPrompt();
        String rawPrompt = info[0];
        String mealType = info[1];
        String formattedPrompt = IRecipeCreator.formatPrompt(mealType, rawPrompt);
        // System.out.println(formattedPrompt);

        return formattedPrompt;
    }

    public void filter() {
        

        if(!filterState.equals("All Recipes")){
            ObservableList<Node> allRecipes = hp.getRecipeList().getChildren();
            ObservableList<Node> filter = FXCollections.observableArrayList();
            allRecipes.forEach((recipe) -> {
                
                if (((RecipeTitle) recipe).getRecipeDetail().getMealType().equals(filterState)){
                    filter.add(recipe);
                }
            });
            hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true);
            hp.getRecipeList().getChildren().addAll(filter);
        }
    }

    public void sort(){
        
        // alphabetical order
        if (sortState.equals("A-Z")) {
            // Create a copy of the children list
            List<Node> sortedList = new ArrayList<>(hp.getRecipeList().getChildren());
    
            // Sort the copy
            Collections.sort(sortedList, new SortAlphabetically());
    
            // Clear the original list and add the sorted elements
            hp.getRecipeList().getChildren().clear();
            hp.getRecipeList().getChildren().addAll(sortedList);
        }
        else if (sortState.equals("Z-A")) {
            // Create a copy of the children list
            List<Node> sortedList = new ArrayList<>(hp.getRecipeList().getChildren());
    
            // Sort the copy
            Collections.sort(sortedList, new SortReverseAlphabetically());
    
            // Clear the original list and add the sorted elements
            hp.getRecipeList().getChildren().clear();
            hp.getRecipeList().getChildren().addAll(sortedList);
        }
        else if (sortState.equals("Default")){
            FXCollections.reverse(hp.getRecipeList().getChildren());
        }
    }

}