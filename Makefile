# author	Taylor Hughes
# date		10/14/2024
# This is used to help build and run Battlecode

EXE = ./gradlew
MGNTA_STRT=\033[1;35m
YLLW_STRT=\033[1;33m
GRN_STRT=\033[1;30m
BLU_STRT=\033[1;34m
CLR_RST=\033[0m
BLD=\033[1m
ACTION_SUCCESS=\033[1;32m+++ Successful Action +++\033[0m
ACTION_FAILURE=\033[1;31m--- Failed Action ---\033[0m

default:
	@echo "Welcome to Makefile"

.SILENT:
runapple: javahomecheck
	@echo "$(GRN_STRT)\nRunning Updates...$(CLR_RST)"
	$(EXE) update
	@echo "$(GRN_STRT)\nPeforming Build...$(CLR_RST)"
	$(EXE) build
	@echo "$(GRN_STRT)\nOpening Battlecode 2024 Client Application...$(CLR_RST)"
	open client/Battle*t.app
	@echo "$(ACTION_SUCCESS)"

help:
	@echo "$(MGNTA_STRT)Makefile Targets: $(CLR_RST)"
	@echo "$(YLLW_STRT)  - runapple $(CLR_RST)............ $(BLD)Updates, Builds, and Opens Battlecode 2024 Application$(CLR_RST)"
	@echo "$(YLLW_STRT)  - opencode $(CLR_RST)............ $(BLD)Opens the project folder in VSCode$(CLR_RST)"
	@echo "$(YLLW_STRT)  - javahomecheck $(CLR_RST)....... $(BLD)Checks whether JAVA_HOME environment variable is set$(CLR_RST)"
	@echo "$(YLLW_STRT)  - runtests $(CLR_RST)............ $(BLD)Assembles test classes and runs test suite, then opens report$(CLR_RST)"
	@echo "$(YLLW_STRT)  - help $(CLR_RST)................ $(BLD)Provides List of Target Commands$(CLR_RST)"

opencode:
	@echo "$(BLU_STRT)Opening Battlecode 2024 Folder in VSCode...$(CLR_RST)"
	code ../Battlecode24-Team1

javahomecheck:
	bash JAVA_HOME.bash

runtests: verification
	@echo "$(GRN_STRT)Opening Test Results ...$(CLR_RST)"
	open -a "Google Chrome" build/reports/tests/test/index.html	

verification:
	@echo "$(GRN_STRT)Assemblying Test Classes ...$(CLR_RST)"
	-./gradlew testClasses
	@echo "$(GRN_STRT)Running Test Suite ...$(CLR_RST)"
	-./gradlew test

.SILENT:
rungame:
	./gradlew run

zip:
	@echo "$(BLU_STRT)Zipping $(CLR_RST)"
	
