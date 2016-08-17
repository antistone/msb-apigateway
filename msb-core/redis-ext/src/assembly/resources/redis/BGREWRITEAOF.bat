@REM
@REM Copyright (C) 2016 ZTE, Inc. and others. All rights reserved. (ZTE)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM
@echo off
title close redis-server
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.

if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set HOME=%DIRNAME%
set _REDISCLIENT=%DIRNAME%redis-cli.exe


echo ================Redis config info =============================================
echo Redis_HOME=$HOME
echo TIP:This shell script rewrite the AOF file in the background！
echo ===============================================================================


cd /d "%HOME%"
echo @WORK_DIR@%HOME%
echo @C_CMD@ "%_REDISCLIENT%  -p 6379 BGREWRITEAOF"
%_REDISCLIENT%  -p 6379 BGREWRITEAOF
echo closing signal has been sent,stopping in background,WAIT...
timeout /t 2 /nobreak > nul  
exit
