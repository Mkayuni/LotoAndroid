# LotaMalawi

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Beta-yellow.svg)]()

<p align="center">
  <img src="https://github.com/user-attachments/assets/[your-unique-image-id]" alt="LotaMalawi Logo" width="200"/>
</p>

## Overview

LotaMalawi is an innovative Android application designed to facilitate the learning of Chichewa, one of Malawi's official languages, through an interactive and gamified experience. Inspired by the popular "Heads Up!" game format, LotaMalawi transforms language acquisition into an engaging activity that can be enjoyed individually or in social settings.

## Table of Contents

- [Features](#features)
- [Technical Architecture](#technical-architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Implementation Details](#implementation-details)
- [Project Structure](#project-structure)
- [Development Roadmap](#development-roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

## Features

### Core Functionality

- **Thematic Category Selection**
  - Users can select from curated thematic categories (Common Phrases, Common Words, Animals)
  - Each category contains contextually relevant English-Chichewa word pairs
  - Modular design allows for easy expansion of categories and vocabulary

- **Motion-Based Interaction**
  - Accelerometer-driven interface utilizing device tilting for hands-free interaction
  - Upward tilt: Skip current word pair
  - Downward tilt: Mark current word pair as correctly guessed
  - Gesture recognition calibrated for optimal user experience

- **Dynamic Scoring System**
  - Real-time tracking of correct guesses and skipped words
  - End-of-round performance summary
  - Storage of gameplay statistics for progress tracking

- **Time-Constrained Gameplay**
  - 30-second gameplay rounds with visual countdown
  - Auditory cues using custom sound effects to indicate time progression
  - Intensity escalation in final seconds to enhance engagement

- **Multi-Sensory Learning Experience**
  - Visual display of word pairs with consistent typography
  - Audio feedback synchronized with gameplay events
  - Animation effects reinforcing user actions and game state transitions

## Technical Architecture

### Technology Stack

- **Frontend**
  - Java 8 for application logic and Android compatibility
  - XML-based layouts with support for both portrait and landscape orientations
  - Dynamic animations via Android Animation Framework
  - Custom view implementations for specialized UI components

- **Backend Services**
  - Asset-based data storage for offline functionality
  - SensorManager integration for motion detection
  - MediaPlayer implementation for audio feedback

- **Development Tools**
  - Android Studio as primary IDE
  - Gradle build system for dependency management
  - Git for version control

### Design Patterns

- **Model-View-Controller (MVC)** architecture separating game logic from presentation
- **Fragment-based UI** architecture for modular screen components
- **Observer Pattern** for sensor event processing
- **Command Pattern** for handling user input

## Installation

### Prerequisites

- Android Studio 4.0 or higher
- Android SDK 21+
- Java Development Kit (JDK) 8+

### Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/username/LotoAndroid.git
   ```

2. Open the project in Android Studio:
   ```bash
   cd LotoAndroid
   ```

3. Sync Gradle and resolve dependencies:
   ```bash
   ./gradlew build
   ```

4. Run the application on an emulator or physical device:
   ```bash
   ./gradlew installDebug
   ```

## Usage

### Getting Started

1. **Launch Application**
   - Open LotaMalawi from your device's application launcher
   - The SplashActivity provides the initial welcome screen

2. **Select Category**
   - Navigate through CategorySelectionActivity to choose from available categories
   - Categories are visually represented with distinctive icons in the UI gallery

3. **Begin Gameplay**
   - Position device on forehead or hold at viewing distance
   - Countdown will begin automatically
   - Word pairs will display in large, readable format

4. **Interaction Mechanics**
   - **Tilt Up**: Skip current word (recorded as "missed")
   - **Tilt Down**: Mark word as correctly guessed (increments score)
   - Motion detection is calibrated for ~9.0 on accelerometer z-axis

5. **Round Completion**
   - Review performance statistics
   - View list of missed words for learning reinforcement
   - Choose to return to category selection or exit

### Advanced Features

- **Slideshow Functionality**: View words in a structured slideshow format
- **Word Pair Reading**: Efficient parsing and display of word pairs from asset files

## Implementation Details

### Key Components

- **MainActivity.java**: Primary control center for application flow
- **CategoryFragment.java**: Handles category selection and user navigation
- **LandscapeActivity.java**: Manages the landscape orientation gameplay
- **WordPairReader.java**: Parses and processes word pairs from text files
- **LOTO.java**: Core game logic implementation
- **SlideshowFragment.java**: UI component for the word slideshow feature

### Sensor Integration

The application utilizes the device's accelerometer sensor to detect tilt gestures:

```java
private SensorEventListener sensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {
        float zValue = event.values[2];

        // Downward tilt (negative z-axis)
        if (zValue < -9.0f) {
            onSkip();
        } 
        // Upward tilt (positive z-axis)
        else if (zValue > 9.0f) {
            onSkipUpwards();
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Accuracy change handling
    }
};
```

### Animation Framework

The application implements custom animations to enhance user experience:

```java
private void startBlinkAnimationForCategory(String category) {
    int animationId = R.anim.fade_blink;

    switch (category) {
        case "Common Phrases":
            applyImageAnimation(R.id.categoryImageCommonPhrases, animationId);
            break;
        case "Common Words":
            applyImageAnimation(R.id.categoryImageCommonWords, animationId);
            break;
        case "Animals":
            applyImageAnimation(R.id.categoryImageAnimals, animationId);
            break;
    }
}
```

### Timer Implementation

The gameplay timer uses a combination of Handler and Runnable for precise timing:

```java
private void startGameCountdown() {
    countdownRunnable = new Runnable() {
        @Override
        public void run() {
            countdownSeconds--;
            countdownTextView.setText(String.valueOf(countdownSeconds));
            
            if (countdownSeconds <= 0) {
                stopTickPlayer();
                displayGameOverAndReturnToCategorySelection();
            } else {
                gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
                playTickPlayer();
                
                // Special handling for final seconds
                if (countdownSeconds == 3) {
                    playDifferentSoundFile();
                }
            }
        }
    };

    gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
}
```

## Project Structure

```
LotoAndroid/
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   │   └── java/com/example/lotoandroid/
│   │   │       └── ExampleInstrumentedTest.java
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   ├── Animals.txt
│   │   │   │   ├── Common Phrases.txt
│   │   │   │   └── Common Words.txt
│   │   │   ├── java/com/example/lotoandroid/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── gallery/
│   │   │   │   │   ├── home/
│   │   │   │   │   └── slideshow/
│   │   │   │   │       ├── SlideshowFragment.java
│   │   │   │   │       └── SlideshowViewModel.java
│   │   │   │   ├── CategoryFragment.java
│   │   │   │   ├── CategorySelectionActivity.java
│   │   │   │   ├── LandscapeActivity.java
│   │   │   │   ├── LOTO.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SplashActivity.java
│   │   │   │   └── WordPairReader.java
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── .gitignore
│   └── build.gradle.kts
├── gradle.properties
└── README.md
```

## Development Roadmap

### Current Status

✓ **Beta Release (99% Complete)**
- Core gameplay mechanics implemented
- Category selection system functional
- Motion detection optimized
- Scoring system operational
- Timer and audio cues integrated

### Upcoming Features

🔄 **Short-term (1-3 months)**
- Enhanced word database with expanded categories
- Performance analytics dashboard
- Adjustable difficulty levels
- UI/UX refinements

🔄 **Mid-term (3-6 months)**
- Multiplayer functionality
- User account system
- Progress tracking and achievements
- Customizable categories

🔄 **Long-term (6+ months)**
- Adaptive learning algorithms
- Integration with external language learning resources
- Cross-platform availability
- Localization for additional interfaces

## Contributing

We welcome contributions to LotaMalawi! Please follow these steps to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines

- Follow the established coding style and patterns
- Write meaningful commit messages
- Add appropriate documentation for new features
- Include unit tests for new functionality

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- The "Heads Up!" game concept for inspiration
- [Android Sensor API](https://developer.android.com/guide/topics/sensors/sensors_overview) documentation
- [Material Design Guidelines](https://material.io/design) for UI principles
- The Chichewa language community for vocabulary verification

---

<p align="center">
  <b>LotaMalawi - Learn Chichewa Through Play</b><br>
  <small>Developed with ❤️ for language enthusiasts</small>
</p>
