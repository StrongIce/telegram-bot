#!/usr/bin/python3.8
# -*- coding: utf-8 -*-
import logging
from aiogram import Bot, Dispatcher
from aiogram.types import BotCommand
from aiogram.contrib.fsm_storage.memory import MemoryStorage
from Handlers.problem_handlers import register_handlers_problem_read
from Handlers.registration_handlers import register_handlers_access
from Handlers.jira_ticket_handlers import register_handlers_admin
from Connections.conn import bot
from aiogram import executor


logger = logging.getLogger(__name__)

WEBHOOK_HOST = 'https://bot.host.ru'
WEBHOOK_PATH = '/webhook/api'
WEBHOOK_URL = f"{WEBHOOK_HOST}{WEBHOOK_PATH}"
WEBAPP_HOST = '127.0.0.1'  # or ip
WEBAPP_PORT = 5000

dp = Dispatcher(bot, storage=MemoryStorage())

file_log = logging.FileHandler('./Logs/log.txt')
console_out = logging.StreamHandler()

logging.basicConfig(handlers=(file_log, console_out), 
                    format='[%(asctime)s | %(levelname)s]: %(message)s', 
                    datefmt='%m.%d.%Y %H:%M:%S',
                    level=logging.INFO)



register_handlers_admin(dp)
register_handlers_problem_read(dp)
register_handlers_access(dp)

async def set_commands(bot: Bot):
    commands = [
        BotCommand(command="/start", description="Начало!"),
        BotCommand(command="/open_ticket", description="Сделать заявку"),
        BotCommand(command="/admin", description="Админка"),
        BotCommand(command="/cancel", description="Стоп!")
    ]
    await bot.set_my_commands(commands)

async def on_startup(dp):
    await bot.set_webhook(WEBHOOK_URL) # Установка вебхука  
    logging.info(bot.get_webhook_info())
    await set_commands(bot) # Установка команд бота


async def on_shutdown(dp):
    await bot.delete_webhook() # Удаление вебхука
    logging.warning('Shutting down..')

    
if __name__ == '__main__':
    executor.start_webhook(
        dispatcher=dp,
        webhook_path=WEBHOOK_PATH,
        on_startup=on_startup,
        on_shutdown=on_shutdown,
        skip_updates=True,
        host=WEBAPP_HOST,
        port=WEBAPP_PORT,
    )