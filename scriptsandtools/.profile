# To the extent possible under law, the author(s) have dedicated all
# copyright and related and neighboring rights to this software to the
# public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along
# with this software.
# If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# base-files version 4.1-1

# ~/.profile: executed by the command interpreter for login shells.

# The latest version as installed by the Cygwin Setup program can
# always be found at /etc/defaults/etc/skel/.profile

# Modifying /etc/skel/.profile directly will prevent
# setup from updating it.

# The copy in your home directory (~/.profile) is yours, please
# feel free to customise it to create a shell
# environment to your liking.  If you feel a change
# would be benificial to all, please feel free to send
# a patch to the cygwin mailing list.

# User dependent .profile file

# Set user-defined locale
export LANG=$(locale -uU)

# This file is not read by bash(1) if ~/.bash_profile or ~/.bash_login
# exists.
#
# if running bash
if [ -n "${BASH_VERSION}" ]; then
  if [ -f "${HOME}/.bashrc" ]; then
    source "${HOME}/.bashrc"
  fi
fi

# for cygwin:

alias ..bash='source ~/.profile'
#alias ..editbash="/cygdrive/c/Tools/Brackets/brackets.exe 'c:\Tools\cygwin\home\MPC\.profile' &"
alias .editbash='nano ~/.profile'
alias ll='ls -la --color=always'
alias .explorer='explorer .'
alias .x='explorer .'
alias .base='cd /home/Mpc/git/base'
alias .kot='cd /home/Mpc/gitkot/kot/kot'
alias .g='gradle --console rich --daemon $*'
alias .gt='gradle --daemon --console rich cleanTest test'
alias ..gbuild='gradle --console rich --daemon build --build-cache $*'
alias ..report='gradle --daemon report'
alias .mariadb='/cygdrive/c/Tools/MariaDb10/bin/mysqld.exe &'

#export WORK_PROJECT='/home/MPC/git/base/www/restgen'
#alias .work='cd $WORK_PROJECT'
#alias .util='cd /home/Mpc/git/base/util'
#alias .nodemon='nodemon'
#alias .jboss='cd /cygdrive/c/tools/jboss-eap-6.2/bin'
#alias .mongodb='/cygdrive/c/Tools/mongodb/bin/mongod --dbpath $(cygpath -aw /cygdrive/c/Tools/mongodb/data/db)'
#alias .mvncleaninstall='mvn clean install'
#alias .nodemon='cd $WORK_PROJECT/angular && nodemon bin/www'
#alias .startApp='cd $WORK_PROJECT && gradle appStart -i'
#alias .appStop='cd $WORK_PROJECT && gradle appStop -i'
#alias .killnodemon='taskkill /F /IM node.exe && taskkill /F /IM node.js'

export HISTFILE="/home/MPC/.bash_history"
export HISTSIZE=100000
export HISTCONTROL="ignoredups"
PROMPT_COMMAND='history -a'
shopt -s histappend
export PATH=/cygdrive/c/Tools/cygwin/home/MPC/bin:$PATH
export CLASSPATH=.:target/app-report-1.0.jar
export CLASSPATH=`cygpath --path --windows "$CLASSPATH"`

# Brackets support ended at adobe
#function .b() {
#    /cygdrive/c/Tools/Brackets/brackets.exe "$*" &
#}

#function br() {
#    $(.b "$*") &
#}

function isadmin()
{
    net session > /dev/null 2>&1
    if [ $? -eq 0 ]; then echo "admin"
    else echo "user"; fi
}

#!/bin/bash
#THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="/home/MPC/.sdkman"
[[ -s "/home/MPC/.sdkman/bin/sdkman-init.sh" ]] && source "/home/MPC/.sdkman/bin/sdkman-init.sh"

