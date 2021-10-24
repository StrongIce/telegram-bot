from aiogram import Bot
from Config.config import *
from jira import JIRA 
from ldap3 import Server, Connection,ASYNC


#API telegram
token = botToken
bot = Bot(token=token,parse_mode='HTML')

#Jira
jira = JIRA(options=jira_server, basic_auth=(jira_login, jira_api_key))



