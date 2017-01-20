#!/bin/bash

# provided for mingw32 gitbash
alias ..bash='source ~/.bashrc'
alias .editbash='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe ~/.bashrc &'
alias ..editbash='/cygdrive/c/Program Files (x86)/JoeEditor/jpico.exe ~/.bashrc'

alias .b='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'
alias br='/c/Program\ Files\ \(x86\)/Brackets/Brackets.exe $* &'

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

function .commit-all-with-message() {

    git commit -a -m "$*"
}

