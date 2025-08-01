
# FoodPlaza Frontend Project Context

## Overview

This project is a JavaFX-based desktop application for managing food plazas. It provides a user interface for administrators, plaza managers, and regular users to interact with the FoodPlaza system. The application is currently in a development phase, transitioning from using mock data to integrating with a FastAPI backend.

## Architecture

The project follows a Model-View-Controller (MVC) architecture:

*   **Model**: The data models are located in `src/main/java/asedi/model` and `src/main/java/asedi/models`. These classes represent the application's data structures, such as `Plaza`, `Local`, and `User`.
*   **View**: The user interface is defined in FXML files located in `src/main/resources/views`. These files describe the layout of the different screens of the application. CSS files for styling are in `src/main/resources/styles`.
*   **Controller**: The controllers in `src/main/java/asedi/controllers` handle the logic for the views. They respond to user input, interact with the services, and update the views.

## Key Technologies

*   **JavaFX**: The application is built using JavaFX for the user interface.
*   **Maven**: The project uses Maven for dependency management and building.
*   **json-simple**: This library is used for parsing JSON data.
*   **FastAPI (Backend)**: The application is designed to integrate with a FastAPI backend, as detailed in the API integration analysis document.

## Project Structure

*   `pom.xml`: Defines the project's dependencies and build configuration.
*   `src/main/java/asedi/Main.java`: The main entry point of the application. It initializes the JavaFX application and loads the login screen.
*   `src/main/java/asedi/controllers/`: Contains the controllers for the different views.
*   `src/main/java/asedi/services/`: Contains the services that provide data to the controllers. Currently, these services use mock data, but they are intended to be updated to interact with the FastAPI backend.
*   `src/main/resources/views/`: Contains the FXML files that define the user interface.
*   `src/main/resources/data/`: Contains JSON files with mock data.
*   `docs/`: Contains documentation, including an analysis of the API integration and a plan for implementing password recovery.

## Functionality

The application has the following features:

*   **User Authentication**: Users can log in with different roles (Admin, Manager, User). The `AuthService` handles authentication.
*   **Admin Dashboard**: Administrators have access to a dashboard where they can manage plazas and locales.
*   **Plaza Management**: The application allows viewing, adding, and (in the future) editing and deleting plazas.
*   **Local Management**: The application allows viewing, adding, and (in the future) editing and deleting locales within a plaza.

## Development Status

The project is in a transitional phase. The frontend is functional but relies on mock data. The next steps, as outlined in the documentation, are to:

1.  Implement an `ApiClient` to communicate with the FastAPI backend.
2.  Update the services to use the `ApiClient` instead of mock data.
3.  Implement the password recovery feature.
4.  Enhance the UI with loading states and error handling for API calls.

This context should provide a good starting point for any LLM to understand the project's structure, functionality, and future direction.
