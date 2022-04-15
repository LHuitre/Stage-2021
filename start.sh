#!/bin/bash

/bin/bash -c 'java -cp ./bot.jar BotSENDHELP.MainBot' > java_server.log 2>&1 &
sleep 15
/bin/bash -c 'python ./frontend/launcherDialogueBot.py'
