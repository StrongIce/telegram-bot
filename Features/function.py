import sqlite3
from ldap3 import  SUBTREE
from Connections.conn import *
from Config.config import *
import bonsai
from bonsai.gevent import GeventLDAPConnection

#Асинхронная функция поиска в Active Directory логина по ФИО
async def login_ldap_search(cn):
    cli = bonsai.LDAPClient(f"ldap://{ldap_srv}")
    cli.set_credentials("SIMPLE", user=f"cn={ldap_user},ou=Служебные,dc=corp,dc=ru", password=f"{ldap_password}")
    cli.set_async_connection_class(GeventLDAPConnection)
    conn = cli.connect(True)
    try:
        res = conn.search("dc=corp,dc=ru", 2,f'(cn={cn}*)')
        login = ''.join(res[0]['sAMAccountName'])
        return login
    except:
        return None

#Добавить информацию о юзере в базу
def add_user_base(user_id,telegram_username,full_name,ldap_username):
    conn = sqlite3.connect('./DB/helpdesk.db')
    cursor = conn.cursor() 
    cursor.execute("INSERT OR REPLACE INTO Users VALUES(?,?,?,?)", (user_id,telegram_username,full_name,ldap_username))
    conn.commit()

def ad_user_base(user_id):
    conn = sqlite3.connect('./DB/helpdesk.db')
    cursor = conn.cursor() 
    cursor.execute(f"SELECT ldap_username FROM Users WHERE user_id={user_id}")
    rows = cursor.fetchall()
    for row in rows:
        return ''.join(row)
    conn.commit()

#Поиск ФИО в базе
def user_fullname_search(user_id):
    conn = sqlite3.connect('./DB/helpdesk.db')
    cursor = conn.cursor()
    cursor.execute(f"SELECT full_name FROM Users WHERE user_id={user_id}")
    rows = cursor.fetchall()
    for row in rows:
        return ''.join(row)
    cursor.close() 

#Проверка наличия  id пользователя в базе
def user_id_search(user_id):
    conn = sqlite3.connect('./DB/helpdesk.db')
    cursor = conn.cursor()
    cursor.execute(f"SELECT user_id FROM Users WHERE user_id={user_id}")
    rows = cursor.fetchall()    
    cursor.close()
    for row in rows:
        return row

def chek_id(user_id):
    id = user_id_search(user_id)
    try:
        if user_id  in id:
            return True
    except:
        return False


# Создание тикета в джире
def create_ticket_jira(problem, fullname, user, description):
    ticket = jira.create_issue(fields={
        'project': {'key': 'IT'},
        'issuetype': {
            "name": "Service Request"
        },
         'reporter':{"name": user},
         'summary': problem,
         'description': description,
         'customfield_10105': fullname,
    })
    return ticket

#Открытые тикеты в джире
def open_tickets(jira):        
    tickets_list = list()
    search = jira.search_issues("status IN ('Открыт')", maxResults=500)
    for ticket in search:
        tickets_list.append(str(ticket))
    return tickets_list

def jira_ticket_info(jira,id):
    issue = jira.issue(f'{id}')
    information = {
        "Тема": f"{issue.fields.summary}",
        "Описание": f"{issue.fields.description}",
        "Создатель": f"{issue.fields.reporter}",
        "Исполнитель": f"{issue.fields.assignee}"
    }
    return information