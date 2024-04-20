all: build

clean: build
	rm -rf .git .gitignore asset README.md lohigh

build: src/main.cpp
	@g++ -o lohigh src/main.cpp
	@./lohigh

config:
	@sudo apt upgrade && sudo apt update && sudo apt autoremove
	@sudo apt-get install libsndfile1-dev
	@sudo apt install gcc g++ clangd

up:
	@git pull 
	@git status