@echo off
echo. 
echo --- Create DataBase----------------------------------------------


SETLOCAL ENABLEDELAYEDEXPANSION
call ..\init.bat

SET CONFIG_FILE=%1
SET NULL_STRING=

IF "%CONFIG_FILE%" NEQ "" (
	%JAVA_CMD% org.ow2.proactive.scheduler.util.CreateDataBase %CONFIG_FILE%
) ELSE (
	echo You must give a configuration file to create database ! Use scheduler_db.cfg as exemple.
)
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------