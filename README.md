# Fuel Price Analysis Project

## Overview

This project aims to analyze and compare fuel prices over time using data from the Orlen website, EIA API, and currency exchange rates.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)

## Requirements

Before using or contributing to this project, make sure you have the following dependencies installed:

- Java Development Kit (JDK)
- ChromeDriver (for Selenium WebDriver)
- [Gson library](https://github.com/google/gson) (for JSON processing)

## Installation

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/your-username/fuel-price-analysis.git
Download ChromeDriver and place it in a directory included in your system's PATH.

Install the Gson library. You can add it to your project using a build tool like Maven or Gradle.

## Usage
Run the Main class to perform fuel price analysis.

The program will fetch oil prices from the EIA API, currency exchange rates, and fuel prices from the Orlen website. It will then analyze and print the results.

## Project Structure

- `project/`: Java source code directory.
  - `API/`: Contains classes for interacting with external APIs.
  - `Selenium/`: Selenium WebDriver implementation for web scraping.
  - `analysis/`: Classes for analyzing and processing data.
- `README.md`: Project documentation.
- `.gitignore`: Specifies intentionally untracked files to ignore in version control.
- `LICENSE`: Project license information.
- `src/`: Additional project files.

