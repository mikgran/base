#!/bin/bash

# provided for mingw32 gitbash
alias ..bash='source ~/.bashrc'
#alias .editbash='/c/Program\ Files\ \(x86\)/Notepad++/notepad++.exe ~/.bashrc'
alias .editbash='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe ~/.bashrc &'

alias .n='/c/Program\ Files\ \(x86\)/Notepad++/notepad++.exe $* &'
alias np='/c/Program\ Files\ \(x86\)/Notepad++/notepad++.exe $* &'
alias .b='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'
alias br='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'

alias .explorer='explorer .'
alias .xplorer='explorer .'
alias .x='explorer .'
alias ll='ls -la'

export WORK_PROJECT='/c/Tools/cygwin/home/MPC/git/base/www/js/angularjava'
export BASE='/c/Tools/cygwin/home/MPC/git/base'
alias .base='cd $BASE'
alias .angularjava='cd $WORK_PROJECT'
alias .react='cd $BASE/www/js'
alias .forever='cd $WORK_PROJECT/angular && forever -w bin/www'
alias .nodemon='cd $WORK_PROJECT/angular && nodemon bin/www'
alias .jetty='cd $WORK_PROJECT && mvn install jetty:run'
alias .killnodemon='taskkill //F //IM node.exe && taskkill //F //IM node.js'

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
        echo "loading ssh-agent"
        ssh-agent -s
    else
        echo "ssh-agent already loaded"
fi

function .killsshexit() {

    kill $(ps | grep -i ssh-agent | awk '{print $1}')
    exit 0
}
