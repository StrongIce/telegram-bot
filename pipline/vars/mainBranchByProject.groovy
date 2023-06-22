String call(String project = 'yyyy_MM_dd') {
    master = 'master'
    // Сопоставление main-веток для репозиториев. По умолчанию main, иначе то,
    // что указано
    branches = [
        'SC.SenseTower.Statistics': master,
        'SC.SenseTower.Twr': master,
        'SC.SenseTower.Halls': master,
        'SC.SenseTower.TowerEvents': master,
        'SC.SenseTower.Cinemas': master,
        'SC.Sensetower.Galleries': master,
        'SC.SenseTower.Images': master,
        'SC.SenseTower.Accounts': master,
        'SC.SenseTower.Tickets': master,
        'SC.ST.News': master,
    ]

    if (branches[project]) {
        return branches[project]
    }
    return 'main'
}
