@echo off
mkdir bin
dir /s /B *.java >> sources.txt
javac -cp YouTubeGo.jar;lib/* -d bin/ @sources.txt
del sources.txt
cd bin
jar -cvf ..\YouTubeGo.jar com