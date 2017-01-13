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
alias .editbash='nano ~/.profile'
alias .xplorer='explorer .'

alias ll='ls -la --color=always'
alias .idea='/cygdrive/c/Program\ Files\ \(x86\)/JetBrains/IntelliJ\ IDEA\ Community\ Edition\ 13.0/bin/idea.exe &'
#alias .n='/cygdrive/c/Program\ Files\ \(x86\)/Notepad++/notepad++.exe $* &'
alias np='run.exe /cygdrive/c/Program\ Files\ \(x86\)/Notepad++/notepad++.exe'

alias .explorer='explorer .'
alias .x='explorer .'
alias .base='cd /home/Mpc/git/base'
alias .angularjava='cd /home/Mpc/git/base/www/js/angularjava/angular'
alias .util='cd /home/Mpc/git/base/util'
alias .nodemon='nodemon'
alias .jboss='cd /cygdrive/c/tools/jboss-eap-6.2/bin'
#alias .mvncleaninstalljettyrun='mvn clean install jetty:run'
alias .basereservation='cd /home/Mpc/git/base/reservation'
alias .mongodb='/cygdrive/c/Tools/mongodb/bin/mongod --dbpath $(cygpath -aw /cygdrive/c/Tools/mongodb/data/db)'
alias .mvntest='echo mvn test -DfailIfNoTests=false -Dtest='
#alias .tc='mvn test -Dtest=CommonTest'
alias .g='gradle --daemon $*'

export SCALA_HOME=/cygdrive/c/Tools/scala-2.10.2
export JBOSS_HOME=/cygdrive/c/tools/jboss-eap-6.2

JAVA_HOME=/cygdrive/c/Tools/java18
export PATH=$JAVA_HOME/bin:$SCALA_HOME/bin:/cygdrive/c/Tools/cygwin/home/MPC/bin:$PATH

export CLASSPATH=.:target/app-report-1.0.jar
export CLASSPATH=`cygpath --path --windows "$CLASSPATH"`

#THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="/home/MPC/.sdkman"
[[ -s "/home/MPC/.sdkman/bin/sdkman-init.sh" ]] && source "/home/MPC/.sdkman/bin/sdkman-init.sh"
