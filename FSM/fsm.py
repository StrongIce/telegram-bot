#Конечные автоматы
from aiogram.dispatcher.filters.state import State, StatesGroup

#Запись проблемы
class ProblemRead(StatesGroup):
    topic = State()
    descrp = State()
    ip = State()
    worker = State()
    balance = State()

class Registration(StatesGroup):
    chek_accses = State()
    access_word = State()
    first_name = State()
    last_name = State()



