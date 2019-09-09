# Seven Seas Wormhole

Wormhole Service for SevenSeas mobile app at wormhole.pirate.black


# Compiling From Source


Install dependencies:

    sudo apt-get install android-sdk gradle

Your system may have a version of gradle that is too old. Gradle 5.6.1 is known
to work well:

    wget https://services.gradle.org/distributions/gradle-5.6.1-bin.zip
    unzip gradle-5.6.1-bin.zip
    export PATH=$HOME/gradle-5.6.1-bin:$PATH

Clone and compile:

    git clone https://github.com/PirateNetwork/SevenSeasWormhole
    cd SevenSeasWormhole
    gradle build
