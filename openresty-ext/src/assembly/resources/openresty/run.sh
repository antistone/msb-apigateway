#!/bin/sh
#
# Copyright (C) 2017 ZTE, Inc. and others. All rights reserved. (ZTE)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DIRNAME=`dirname $0`
HOME=`cd $DIRNAME/nginx; pwd`
_NGINXCMD="$HOME/sbin/nginx"
LUAJIT_HOME=`cd $DIRNAME/luajit; pwd`
echo =========== prepare the symbolic links  ========================================
sudo ln -s -f $_NGINXCMD $DIRNAME/bin/openresty
sudo ln -s -f $LUAJIT_HOME/bin/luajit2.1.0-beta2 $LUAJIT_HOME/bin/luajit
sudo ln -s -f $LUAJIT_HOME/lib/libluajit-5.1.so.2.1.0 $LUAJIT_HOME/lib/libluajit-5.1.so.2
sudo ln -s -f $LUAJIT_HOME/lib/libluajit-5.1.so.2.1.0 $LUAJIT_HOME/lib/libluajit-5.1.so
echo ================================================================================

echo =========== create symbolic link for libluajit-5.1.so.2  ========================================
LUAJIT_HOME=`cd $DIRNAME/luajit; pwd`
LUAJIT_FILENAME="$LUAJIT_HOME/lib/libluajit-5.1.so.2"
LN_TARGET_FILE='/lib/libluajit-5.1.so.2'
LN_TARGET_FILE64='/lib64/libluajit-5.1.so.2'
sudo ln -s -f $LUAJIT_FILENAME $LN_TARGET_FILE
sudo ln -s -f $LUAJIT_FILENAME $LN_TARGET_FILE64
echo ===============================================================================

echo =========== openresty config info  =============================================
echo HOME=$HOME
echo _NGINXCMD=$_NGINXCMD
echo ===============================================================================
cd $HOME; pwd

echo @WORK_DIR@ $HOME
echo @C_CMD@ $_NGINXCMD -p $HOME/
sudo $_NGINXCMD -p $HOME/

