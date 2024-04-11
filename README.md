# PantryPal
> PantryPal created by GitHub Classroom


## Link our project's Github repo 

**Our project's Github repo [here](https://github.com/ucsd-cse110-fa23/cse-110-project-team-11).**

## Setup Instructions
To get started with the project, follow these steps:

1. Clone the repository:
<img width="700" alt="Screenshot 2023-11-30 at 4 19 44 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/2a9c1507-1f2c-41ef-9fd4-e123843e412c">

2. **Open the project on Visual Studio Code**


## Running the App

### Visual Studio Code Setup
To run the app in Visual Studio Code, follow these steps:

1. **Launch the Server:**

<img width="333" alt="Screenshot 2023-11-30 at 1 01 57 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/ccd04d71-59ac-42dd-9634-8dcc0b9402ae">

2. **Run the App:**
   
Depends on if you are `Mac` or `Window`, choose either `App_mac` or `App_win`


<img width="278" alt="Screenshot 2023-11-30 at 1 03 18 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/0c84e73d-de51-4e1d-a3a0-e542b4e86542">

## Expected Output
Here's what you should see when you run the app:

<img width="302" alt="Screenshot 2023-12-04 at 6 37 33 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/d192d574-5a80-485d-8872-50f79eedf3d6">



## Common Issues

### 1. JavaFX Configuration ( Common for M2 Users )

When you see following lines in terminal after launching  `App_mac` or `App_win`: 


```
Graphics Device initialization failed for :  es2, sw
Error initializing QuantumRenderer: no suitable pipeline found
```

1. Download JavaFX [here](https://gluonhq.com/products/javafx/).
2. Replace the current lib with the JavaFX `lib` directory that you just downloaded.

<img width="404" alt="Screenshot 2023-11-30 at 1 27 26 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/3a2b04c8-f7cc-4254-a2f7-63fa0892324d">

### 2. Missing Resource Folder

If you find this error from terminal when creating recipe

```
java.io.FileNotFoundException: src/main/resources/recipe.txt (No such file or directory)
```

Simply add a `resources` folder under main.

![Screenshot 2023-12-04 at 6 44 04 PM](https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/34fe6935-e154-4bb7-9356-c3aa8c815752)

Expected Format: 

<img width="230" alt="Screenshot 2023-12-04 at 6 45 26 PM" src="https://github.com/ucsd-cse110-fa23/cse-110-project-team-11/assets/111910985/a15c5846-922f-49a8-b1e3-3a09ef7b9186">

---
cse-110-project-team-11 © 2023
