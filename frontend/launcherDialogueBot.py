import discord
import os

from dotenv import load_dotenv
from py4j.java_gateway import JavaGateway

load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')
CLIENT_ID = os.getenv('DISCORD_CLIENT_ID')
CLIENT_ID = int(CLIENT_ID)

gateway = JavaGateway()
client = discord.Client()

clean_app = gateway.entry_point

state = "base"
ignoreRel = True
activateInf = False

#détecte si l'utilisateur tutoies le bot
def tuInMessage(message):
    return (message.lower()[0:3] == "tu "
          or " tu " in message
          or "-tu " in message)

#échange utilisé pour lever l'ambiguité sur les mots-noyaux
def repUser(res):
    global state, ignoreRel, activateInf
    if res != "":
        state = "rep_user_mainMWE"
        return res
    else:
        res = clean_app.test(ignoreRel, activateInf)
        state = "rep_user"
        return "C'est bon pour moi !\n" + res

#exécution de la vérification d'un triplet par inférence ou pas
def searchRelInf(message):
    global state, ignoreRel, activateInf
    if(clean_app.confirmationMainMWE(message)):
        res = clean_app.askConfirmationMainMWE()
        newRes = repUser(res)
        return newRes
    else:
        res = clean_app.test(ignoreRel, activateInf)
        state = "rep_user"
        return res



@client.event
async def on_ready():
    print('We have logged in as {0.user}'.format(client))
    
    hello = "Bonjour ! Je suis SENDHELP, le Système d'Extraction de Nouvelles Données avec Heuristique d'Exploration du Langage par Palabres. On peut se tutoyer ! "
    hello += "N'hésite pas à me poser une question, j'essaierai d'y répondre de mon mieux. Il est aussi possible que j'en ai, n'hésite pas à me demander !"
    hello += '\nTapez **$help** pour afficher l\'aide :wink:'
    await client.get_channel(CLIENT_ID).send(hello)

@client.event
async def on_message(message):
    global state, ignoreRel, activateInf
    
    if message.author == client.user:
        clean_app.writeLog(message.content, "SENDHELP")
        return
    else:
        clean_app.writeLog(message.content, "USER")

    if state == "ask_mainMWE":
        res = clean_app.askConfirmationMainMWE()
        newRes = repUser(res)
        await message.channel.send(newRes)

    elif state == "rep_user_mainMWE":
        temp = (message.content)
        res = clean_app.verifyReponseMainMWE(temp)
        if res == False:
            await message.channel.send("Ta réponse n'est pas valide !")
        else:
            state = "ask_mainMWE"
            await message.channel.send("Merci !")
            
            res = clean_app.askConfirmationMainMWE()
            newRes = repUser(res)
            await message.channel.send(newRes)

    elif message.content.startswith("$!i "):
        temp = (message.content)[len("$!i ")::]
        ignoreRel, activateInf = False, False
        res = searchRelInf(temp)
        await message.channel.send(newRes)

    elif message.content.startswith("$i ") or message.content.lower().startswith("est-ce que ") or message.content.lower().startswith("est-ce qu'"):
        if message.content.startswith("$i "): temp = (message.content)[len("$i ")::]
        elif message.content.lower().startswith("est-ce que "): temp = (message.content)[len("est-ce que ")::]
        elif message.content.lower().startswith("est-ce qu'"): temp = (message.content)[len("est-ce qu'")::]
        ignoreRel, activateInf = False, True
        res = searchRelInf(temp)
        await message.channel.send(res)

    elif message.content.startswith("$!rel ") or message.content.lower().startswith("pourquoi "):
        if message.content.startswith("$!rel "): temp = (message.content)[len("$!rel ")::]
        elif message.content.lower().startswith("pourquoi "): temp = (message.content)[len("pourquoi ")::]

        #Le message est trop petit pour être une question développée
        if len(temp.split(" ")) <= 3:
            res = clean_app.whyTriplet()
            state = "base"
        else:
            ignoreRel, activateInf = True, True
            res = searchRelInf(temp)
        
        await message.channel.send(res)

    elif message.content.startswith("$data"):
        pathFile = clean_app.getNewTriplets()
        await message.channel.send("Voici le fichier contenant les nouvelles relations que j'ai trouvées.")
        await message.channel.send(file=discord.File(pathFile, filename="newTriplets.txt"))

    elif message.content == "$help":
        helpMessage = 'Pour télécharger le fichier contenant les nouvelles relations extraites, écrivez **$data**'
        helpMessage += '\nPour insérer une nouvelle relation directement, utilise une formule du style **Sais-tu que...** ou **Retiens que...**.'
        helpMessage += '\nPour poser ta question, utilise une formulation commençant par **Est-ce que** (équivalent à **$i**). '
        helpMessage += 'Une formulation commençant par **Pourquoi** (équivalent à **$!rel**) essaiera de donner une justification sans faire de tautologie.'
        helpMessage += '\nPour demander une justification concernant la précédente question, écrive tout simplement **Pourquoi**.'
        helpMessage += '\nPour faire une requête, écris : ```<select> <mot 1> <nom de la relation> <mot 2>```'
        helpMessage += 'Le champ **<select>** peut être :'
        helpMessage += "\n\t - **$i** pour faire la requête avec inférences ;"
        helpMessage += "\n\t - **$!i** pour faire la requête sans inférences ;"
        helpMessage += "\n\t - **$!rel** pour faire la requête en ignorant une potentielle relation directe entre les termes."
        helpMessage += "\nPar exemple :```Est-ce qu'un chien peut mordre ? \n$i chien r_agent-1 mordre```"
        helpMessage += "ou encore :```Pourquoi un chien peut mordre ? \n$!rel chien r_agent-1 mordre```"
        helpMessage += "\nIl est aussi possible que j'ai des questions à te poser, n'hésite pas à me le demander directement !"
        await message.channel.send(helpMessage)

    elif " question" in message.content and tuInMessage(message.content):
        res = clean_app.botHasQuestionByUser()
        state = "rep_user"
        await message.channel.send(res)
            
    elif ((("savais" in message.content.lower() or "sais" in message.content.lower()) and tuInMessage(message.content))
          or "retiens que " in message.content.lower()
          or "retiens qu'" in message.content.lower()
          or "retiens le fait que " in message.content.lower()
          or "retiens le fait qu'" in message.content.lower()):
        temp = message.content.split(" que ", 1)[-1]
        if(temp == message.content):
            temp = message.content.split(" qu'", 1)[-1]
        if(temp != message.content):
            res = clean_app.newFact(temp)
            await message.channel.send(res)

    elif state == "rep_user":
        if "pourquoi" in message.content.lower():
            ignoreRel, activateInf = True, True
            res = clean_app.whyTriplet()
            state = "base"
            await message.channel.send(res)
        else:
            resUser = clean_app.parseTrueFalseUser(message.content)
            resBot = clean_app.addNewAnswer(resUser)
            state = "base"
            await message.channel.send(resBot)

client.run(TOKEN)
