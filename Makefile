all: build

clean: build
	rm -rf .git .gitignore asset README.md lohigh src/*.class

build: src/Main.java
	@javac src/Main.java
	@echo "Build complete! Run with: java -cp src Main <input.wav> <output.wav>"

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