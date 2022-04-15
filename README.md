# Bot SENDHELP (Discord)

## Lancement

Lancer la JVM par le fichier MainBot.java (package BotSENDHELP)
Lancer le fichier python frontend/launcherDialogueBot.py

## Maven

Pour produire le jar avec les dépendances : 

```bash
mvn clean package
```

Pour lancer :

```bash
java -cp ./target/bot_sendhelp-0.0.1-SNAPSHOT-jar-with-dependencies.jar BotSENDHELP.MainBot
```

## Docker

```bash
docker build -t bot_sendhelp .
docker run -d bot_sendhelp
```
