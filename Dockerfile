#
# Build stage
#
FROM maven:3.8.1-jdk-11-slim AS build
COPY . /bot_build
RUN mvn -f /bot_build/pom.xml clean package

FROM openjdk:11-jre-slim
COPY --from=build /bot_build/target/bot_sendhelp-0.0.1-SNAPSHOT-jar-with-dependencies.jar /bot/bot.jar
COPY --from=python:3.8 / /
WORKDIR /bot

COPY ./frontend /bot/frontend
COPY start.sh .
COPY ./txtData /bot/txtData
COPY ./resources /bot/resources
COPY ./generatedData /bot/generatedData

RUN pip install -r frontend/requirements.txt
RUN chmod +x start.sh

# CMD [ "java", "-cp", "bot.jar BotSENDHELP.MainBot"]
# CMD java -cp bot.jar BotSENDHELP.MainBot & ; python frontend/launcherDialogueBot.py 
CMD ./start.sh

