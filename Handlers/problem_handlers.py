from aiogram import Dispatcher, types
from aiogram.dispatcher import FSMContext
from FSM.fsm import ProblemRead,Registration
from Keyboards.keyboards import keyboard_problem
from Connections.conn import bot
from Features.function import *
from Config.config import group_chat

async def cancel(message: types.Message, state=FSMContext):
    await message.answer("<em>Действие отменено!</em>",reply_markup=types.ReplyKeyboardRemove())
    await state.finish()

async def start(message: types.Message):
    if chek_id(message.from_user.id) != True:
        await message.answer(f"Нет доступа, воспользуйся командой /start")
    else:
        await message.answer(f'<em>Что стряслось?</em>',reply_markup=keyboard_problem)
        await ProblemRead.topic.set()

async def descrp(message: types.Message, state=FSMContext):
    topic = ('Телефония','Компьютер','Интернет','Нокс','Баланс','Другое')
    if message.text not in topic:
        await message.answer('<em>Выбери тему используя кнопки!</em>')
        await ProblemRead.topic.set()
        
    if message.text == 'Баланс':
        await state.update_data(balance=message.text)
        await message.answer('<em>Кратко опиши проблему: </em>',reply_markup=types.ReplyKeyboardRemove())
        await ProblemRead.balance.set()

    if message.text in topic and message.text != "Баланс":    
        await state.update_data(topic=message.text)
        await ProblemRead.descrp.set()
        await message.answer(f'<em>Кратко опиши проблему: </em> ',reply_markup=types.ReplyKeyboardRemove())

async def worker(message: types.Message, state=FSMContext):
    await state.update_data(descryption=message.text)
    await ProblemRead.worker.set()
    await message.answer('<em>Сотрудник: </em>')

async def ip(message: types.Message, state=FSMContext):
    await state.update_data(descryption=message.text)
    await ProblemRead.ip.set()
    await message.answer('<em>Введи свой IP или AnyDesk</em>')

async def creat_ticket_balance(message: types.Message, state=FSMContext):
    user_date = await state.get_data()
    topic = user_date['balance']
    problem = f"{user_date['descryption']}\nСотрудник: {message.text}"
    name = user_fullname_search(message.from_user.id)
    ldap_user = await login_ldap_search(name)
    ticket = create_ticket_jira(problem=topic,fullname=str(name),description = problem,user=ldap_user)
    await bot.send_message(group_chat,
                         f"Заявка создана от: <u><b>{name}</b></u> \nПрофиль телеграмм: @{message.from_user.username}"
                         f"\nТема: {user_date['balance']} \nОписание: {user_date['descryption']}"
                         f"\nСотрудник: {message.text} \nC номером: {ticket}")                 
    await message.answer(f"<em>Твоя заявка: {ticket} создана!</em>\n"
                         f"<em>Тема: {user_date['balance']}</em>\n"
                         f"<em>Описание: {user_date['descryption']}</em>\n"
                         f"<em>Сотрудник: {message.text}</em>")
    await state.finish()

async def creat_ticket(message: types.Message, state=FSMContext):
    ip = message.text
    user_date = await state.get_data()
    topic = user_date['topic']
    problem = f"{user_date['descryption']}\nIP: {ip}"
    name = user_fullname_search(message.from_user.id)
    ldap_user = await login_ldap_search(name)
    ticket = create_ticket_jira(problem=topic,fullname=str(name),description = problem,user=ldap_user)
    await bot.send_message(group_chat,
                         f"Заявка создана от: <u><b>{name}</b></u> \nПрофиль телеграмм: @{message.from_user.username}"
                         f"\nТема: {user_date['topic']} \nОписание: {user_date['descryption']}"
                         f"\nIP: {ip} \nC номером: {ticket}")                 
    await message.answer(f"<em>Твоя заявка: {ticket} создана!</em>\n"
                         f"<em>Тема: {user_date['topic']}</em>\n"
                         f"<em>Описание: {user_date['descryption']}</em>\n"
                         f"<em>IP: {ip}</em>")
    await state.finish()




def register_handlers_problem_read(dp: Dispatcher):
    dp.register_message_handler(start, commands="open_ticket", state="*",chat_type='private')
    dp.register_message_handler(cancel, commands="cancel", state=[ProblemRead.balance,
                                                                 ProblemRead.topic,
                                                                 ProblemRead.descrp,
                                                                 ProblemRead.ip,
                                                                 Registration.access_word,
                                                                 Registration.access_word,
                                                                 Registration.first_name,
                                                                 Registration.last_name,
                                                                 ProblemRead.worker],chat_type='private')
    dp.register_message_handler(descrp,state=ProblemRead.topic)
    dp.register_message_handler(ip,state=ProblemRead.descrp)
    dp.register_message_handler(creat_ticket,state=ProblemRead.ip)
    dp.register_message_handler(worker,state=ProblemRead.balance)
    dp.register_message_handler(creat_ticket_balance,state=ProblemRead.worker)
    