package pantryPal;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.util.*;

import pantryPal.View.HomePageAppFrame;
import pantryPal.View.HomePageHeader;
import pantryPal.View.InputAppFrame;
import pantryPal.View.RecipeDisplayAppFrame;
import pantryPal.View.RecButtons;
import pantryPal.View.RecipeDisplay;
import pantryPal.View.RecipeTitle;
import pantryPal.View.UI;
public class Controller {
    
    private Input input = new Input();
    private RecipeCreator rc = new RecipeCreator();
    private InputAppFrame inputFrame;
    private RecipeParser rp = new RecipeParser();
    private UI ui;
    private HomePageHeader hph;
    private HomePageAppFrame hp;
    private RecipeDisplayAppFrame rd;
    private RecipeTitle rt = new RecipeTitle("");
    private Model model;

    public Controller(UI ui, Model model) {
        this.model = model;
        this.ui = ui;
        this.inputFrame = ui.getInputPage();
        this.hp = ui.getHomePage();
        this.hph = hp.getHomePageHeader();
        this.rd = ui.getDisplayPage();        
        
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
        this.rd.setSaveButtonAction(this::handleSaveButton);
        this.rd.setEditButtonAction(this::handleEditButton);
        this.rt.setViewButtonAction(this::handleViewButton);
    }

    public void handleCreateButton(ActionEvent event) {
        ui.getRoot().setCenter(inputFrame);
        ui.getRoot().setTop(inputFrame.getReturnHeader());
    }

    public void handleStartButton(ActionEvent event) {
        RecButtons rb = inputFrame.getRecButtons();
        rb.setRecordingLabel("Recording");
        input.captureAudio();
        inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStartButton());
        inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStopButton());
    }
    // TODO auto stop when press back

    public void handleStopButton(ActionEvent event) throws InterruptedException, IOException {
        // Stop Button

        String promptType = input.getPromptType();
        inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStopButton());
        inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStartButton());
        
        if(input.stopCapture(promptType)){
            if(promptType.equals("MealType")){
                input.setPromptType("Ingredients");
                inputFrame.getRecButtons().setRecipeText("");
                inputFrame.getRecButtons().setRecordingLabel("Please input Ingredients");
            }
            else{
                inputFrame.getRecButtons().setRecordingLabel("Recipe Displayed");
                RecipeDisplay rec = new RecipeDisplay();
                input.setPromptType("MealType");
                rc.generateRecipe();
                try {
                    rp.parse(); 
                    rec.setID(null);
                    rec.setTitle(rp.getTitle());
                    rec.setIngreds(rp.getStringIngredients());
                    rec.setSteps(rp.getStringSteps());
                    System.out.println(rec.getIngredients().getText());
                    System.out.println(rec.getSteps().getText());
                    System.out.println("SDUHFIOSDHFIOSHDOFHSDIOFHSDIOFSIDHFOSDIFHSODi");
                    RecipeDisplayAppFrame displayRec = new RecipeDisplayAppFrame(rec);
                    displayRec.setBackButtonAction2(this::handleBackButton2);
                    displayRec.setDeleteButtonAction(this::handleDeleteButton);
                    displayRec.setSaveButtonAction(this::handleSaveButton);
                    displayRec.setEditButtonAction(this::handleEditButton);
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
        }
        else{
            inputFrame.getRecButtons().setRecordingLabel("Invalid Input. Please say a proper meal type");
        }
        
    }

    private void handleBackButton(ActionEvent event){
        ui.returnHomePage();   
        input.setPromptType("MealType"); 
        inputFrame.getRecButtons().setRecordingLabel("Select Meal Type: Breakfast, Lunch, or Dinner");  
        if(input.getMic() != null){
            input.getMic().stop();
            input.getMic().close();
        }  
        if (inputFrame.getRecButtons().getButtonBox().getChildren().contains(inputFrame.getRecButtons().getStopButton())){
            System.out.println("TEST");
            inputFrame.getRecButtons().getButtonBox().getChildren().remove(inputFrame.getRecButtons().getStopButton());
            inputFrame.getRecButtons().getButtonBox().getChildren().add(inputFrame.getRecButtons().getStartButton());
        }
    }

    private void handleBackButton2(ActionEvent event){
        ui.returnHomePage();   
        input.setPromptType("MealType"); 
        inputFrame.getRecButtons().setRecordingLabel("Select Meal Type: Breakfast, Lunch, or Dinner");    
        if(this.rd.getEditable()){
            RecipeDisplayAppFrame r = this.rd;
            TextArea ingredients = r.getIngredients();
            Button editButton = r.getEditButton();
            TextArea steps = r.getSteps();
            ingredients.setEditable(false);
            steps.setEditable(false);
            editButton.setText("Edit");
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
                editButton.setText("Stop");
                rd.setEditable(true);
            }
        else {
            ingredients.setEditable(false);
            steps.setEditable(false);
            editButton.setText("Edit");
            rd.setEditable(false);
            reload();
            ui.returnHomePage();
        }
    }

    public void handleSaveButton(ActionEvent event) {
        rd.getIngredients().setEditable(false);
        rd.getSteps();
        if (rd.getID() == null) { // if does not exist in MongoDB (?)
            // String[] recipe = RecipeManager.insertRecipe(rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText());
            String[] recipe = model.performRequest("PUT", rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText(), rd.getID());
            String stringID = recipe[0];
            String title = recipe[1];
            String ingredients = recipe[2];
            String steps = recipe[3];
            RecipeDisplay recipeDisplay = new RecipeDisplay(stringID, title, ingredients, steps);
            RecipeDisplayAppFrame rec = new RecipeDisplayAppFrame(recipeDisplay);
            RecipeTitle recipeDis = new RecipeTitle(stringID, title, rec);
            model.performRequest("PUT", rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText(), rd.getID());
            rd.setID(RecipeManager.getStringID());
            recipeDis.setViewButtonAction(this::handleViewButton);
            recipeDis.getRecipeDetail().setBackButtonAction2(this::handleBackButton2);
            this.rt = recipeDis;
            hp.getRecipeList().getChildren().add(recipeDis);
            reload();
        }
        else {
            try {
                RecipeManager.updateRecipe(rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText(), rd.getID());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private void handleViewButton(ActionEvent event){
   
        ui.getRoot().setCenter(this.rt.getRecipeDetail()); 
        ui.getRoot().setTop(this.rt.getRecipeDetail().getRecipeDisplayHeader());
    }

    private void handleDeleteButton(ActionEvent event) {
        System.out.println("HELLOOO");
        model.performRequest("DELETE", rd.getTitle().getText(), rd.getIngredients().getText(), rd.getSteps().getText(), rd.getID());
        reload();
        ui.returnHomePage();
    }

    public void reload(){
        hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true);  
        loadRecipes(); // loads recipe
    }

    public void loadRecipes(){
        hp.getRecipeList().getChildren().removeIf(RecipeTitle -> RecipeTitle instanceof RecipeTitle && true); 
        ArrayList<String[]> recipes = RecipeManager.loadRecipes();
        
        for(int i = 0; i < recipes.size(); i++){

            String stringID = recipes.get(i)[0];
            String title = recipes.get(i)[1];
            String ingredients = recipes.get(i)[2];
            String steps = recipes.get(i)[3];
            RecipeDisplay recipeDisplay = new RecipeDisplay(stringID, title, ingredients, steps);
            RecipeDisplayAppFrame rec = new RecipeDisplayAppFrame(recipeDisplay);
            RecipeTitle recipeTitle = new RecipeTitle(stringID, title, rec);
            //recipes.get(i).setViewButtonAction(this::handleViewButton);
            // RecipeTitle title = recipes.get(i);
            //System.out.println(title.getID());
            // RecipeDisplayAppFrame recDisp = title.getRecipeDetail();
            rec.setID(recipeTitle.getID());
            // System.out.println(rec.getID());
           recipeTitle.getViewButton().setOnAction(e1->{
                ui.getRoot().setCenter(recipeTitle.getRecipeDetail()); 
                ui.getRoot().setTop(recipeTitle.getRecipeDetail().getRecipeDisplayHeader());
                this.rd = rec;

                rec.setEditButtonAction(this::handleEditButton);
                rec.setSaveButtonAction(this::handleSaveButton);
                rec.setDeleteButtonAction(this::handleDeleteButton);

            });
            
            hp.getRecipeList().getChildren().add(recipeTitle);
            recipeTitle.getRecipeDetail().setBackButtonAction2(this::handleBackButton2);
        }

    }



}