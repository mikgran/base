#!/bin/sh

alias ..bash='source ~/.bashrc'
alias ..editbash='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe ~/.bashrc &'
alias .editbash='nano.exe ~/.bashrc'
#alias .editbash='/c/Tools/emacs/bin/emacs -mm ~/.bashrc &'
#alias emacs='/c/Tools/emacs/bin/emacs -mm'
#alias ec='/c/Tools/emacs/bin/emacs -mm'
#alias .b='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'
#alias br='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'

alias .explorer='explorer .'
alias .xplorer='explorer .'
alias .x='explorer .'
alias ll='ls -la --color=always'

export WORK_PROJECT='/c/Tools/cygwin/home/MPC/git/base/www/js/angularjava'
export BASE='/c/Tools/cygwin/home/MPC/git/base'
alias .base='cd $BASE'
alias .angularjava='cd $WORK_PROJECT'
alias .react='cd $BASE/www/js'
alias .nodemon='cd $WORK_PROJECT/angular && nodemon bin/www'
alias .jetty='cd $WORK_PROJECT && mvn install jetty:run'
#alias .killnodemon='taskkill //F //IM node.exe && taskkill //F //IM node.js'

#alias .commit-all-with-message='git commit -a -m $*'
alias ..commitall='git commit -a'
alias .commit='git commit'
alias .fetch='git fetch'
alias .pull='git pull origin master'
alias .push='git push origin master'
alias st='git status'
alias slog='git log --stat'
alias diff='git diff'
alias .mvninstallskiptests='mvn install -skipTests=true'

export SSH_AGENT_NOT_FOUND=$(ps | grep -i ssh-agent)
if [ "x$SSH_AGENT_NOT_FOUND" == "x" ];
    then
        echo "loading ssh agent"
        ssh-agent -s
    else
        echo "ssh agent already loaded"
fi

function .killsshexit() {

    kill $(ps | grep -i ssh-agent | awk '{print $1}')
    exit 0
}

export STOOLS='/c/Tools/cygwin/home/MPC/git/base/scriptsandtools'
export MSYSGIT_MPC_HOME='/c/Users/Mpc'
export CYGWIN_MPC_HOME='/c/Tools/cygwin/home/Mpc'

function ..copy-msysgit-and-cygwin-init-files-to-scriptsandtools() {

    cp $MSYSGIT_MPC_HOME/.bashrc $STOOLS
    cp $CYGWIN_MPC_HOME/.profile $STOOLS
}

function ..get-msysgit-and-cygwin-init-files-from-scriptsandtools() {

    cp $MSYSGIT_MPC_HOME/.bashrc $MSYSGIT_MPC_HOME/.bashrc.old
    cp $CYGWIN_MPC_HOME/.profile $CYGWIN_MPC_HOME/.profile.old
    cp $STOOLS/.bashrc $MSYSGIT_MPC_HOME
    cp $STOOLS/.profile $CYGWIN_MPC_HOME
}

function .b() {
    brackets "$*" &
}

function br() {
    $(.b "$*") &
}

function .striptrailingwhitespace() {
    sed -i 's/[[:space:]]*$//' "$*"
}

function .dos2unixcygwindotprofile() {
    dos2unix /c/Tools/cygwin/home/MPC/.profile
}
