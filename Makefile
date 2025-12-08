all: build

clean: build
	rm -rf .git .gitignore asset README.md lohigh src/*.class

build: src/Main.java
	@javac src/Main.java
	@echo "Build complete! Run with: java -cp src Main <input.wav> <output.wav>"

debug: src/Main.java
	@echo "Building in debug mode with verbose compiler output..."
	@javac -g -verbose src/Main.java
	@echo "Debug build complete!"
	@echo "Debug symbols included. Run with verbose flag: java -cp src Main <args> -v"
	@echo "For extra debugging, run with: java -Xdebug -cp src Main <args>"

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

.PHONY: all clean build debug run config up