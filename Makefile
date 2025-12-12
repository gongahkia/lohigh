all: build

clean: build
	rm -rf .git .gitignore asset README.md lohigh src/*.class

build: src/*.java
	@javac src/*.java
	@echo "Build complete! Run with: java -cp src Main <input.wav> <output.wav>"

debug: src/*.java
	@echo "Building in debug mode with verbose compiler output..."
	@javac -g -verbose src/*.java
	@echo "Debug build complete!"
	@echo "Debug symbols included. Run with verbose flag: java -cp src Main <args> -v"
	@echo "For extra debugging, run with: java -Xdebug -cp src Main <args>"

jar: build
	@echo "Creating JAR package..."
	@echo "Main-Class: Main" > manifest.txt
	@jar cfm lohigh.jar manifest.txt -C src Main.class
	@rm manifest.txt
	@echo "JAR created successfully: lohigh.jar"
	@echo "Run with: java -jar lohigh.jar <input.wav> <output.wav>"
	@echo "Or: ./lohigh.jar <input.wav> <output.wav> (if executable permission set)"

jar-with-assets: build
	@echo "Creating standalone JAR with embedded assets..."
	@echo "Main-Class: Main" > manifest.txt
	@mkdir -p build
	@cp -r src/*.class build/
	@cp -r asset build/
	@jar cfm lohigh-standalone.jar manifest.txt -C build .
	@rm -rf build manifest.txt
	@echo "Standalone JAR created: lohigh-standalone.jar"
	@echo "This JAR includes the ambient.wav asset file."
	@echo "Run with: java -jar lohigh-standalone.jar <input.wav> <output.wav>"

install-jar: jar
	@echo "Installing lohigh.jar to /usr/local/bin..."
	@sudo cp lohigh.jar /usr/local/lib/lohigh.jar
	@echo '#!/bin/bash' | sudo tee /usr/local/bin/lohigh > /dev/null
	@echo 'java -jar /usr/local/lib/lohigh.jar "$$@"' | sudo tee -a /usr/local/bin/lohigh > /dev/null
	@sudo chmod +x /usr/local/bin/lohigh
	@echo "Installation complete! Run with: lohigh <input.wav> <output.wav>"

install-man: man/lohigh.1
	@echo "Installing man page..."
	@sudo mkdir -p /usr/local/share/man/man1
	@sudo cp man/lohigh.1 /usr/local/share/man/man1/
	@sudo chmod 644 /usr/local/share/man/man1/lohigh.1
	@echo "Man page installed! View with: man lohigh"

install: install-jar install-man
	@echo "Full installation complete!"
	@echo "Run with: lohigh <input.wav> <output.wav>"
	@echo "View manual: man lohigh"

uninstall-jar:
	@echo "Uninstalling lohigh..."
	@sudo rm -f /usr/local/lib/lohigh.jar
	@sudo rm -f /usr/local/bin/lohigh
	@echo "Uninstall complete."

uninstall-man:
	@echo "Uninstalling man page..."
	@sudo rm -f /usr/local/share/man/man1/lohigh.1
	@echo "Man page uninstalled."

uninstall: uninstall-jar uninstall-man
	@echo "Full uninstall complete."

run: build
	@java -cp src Main

config:
	@echo "Checking for Java installation..."
	@java -version
	@javac -version
	@echo "Java is ready! No additional dependencies needed."

up:
	@git pull
	@git status

.PHONY: all clean build debug jar jar-with-assets install-jar install-man install uninstall-jar uninstall-man uninstall run config up