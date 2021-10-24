from aiogram import types
from Features.function import open_tickets
from Connections.conn import jira

keyboard_problem = types.ReplyKeyboardMarkup(resize_keyboard=True,row_width=2)
keyboard_problem.add('Телефония','Компьютер','Интернет','Нокс','Баланс','Другое')

keyboard_access = types.ReplyKeyboardMarkup(resize_keyboard=True) 
keyboard_access.add("У меня есть доступ!","Я не из вашей компании!")

keyboard_ticket = types.InlineKeyboardMarkup(row_width=4)
keyboard_ticket_button = (types.InlineKeyboardButton(data, callback_data=data) for data in open_tickets(jira))
keyboard_ticket.add(*keyboard_ticket_button)