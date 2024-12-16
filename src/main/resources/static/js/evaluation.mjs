import {API_URL} from "/js/environment.mjs";

document.addEventListener("DOMContentLoaded", function () {
    const evaluationId = getEvaluationIdFromUrl();

    Promise.all([
        getEvaluationData(evaluationId),
        getRanksData(),
        getActionsData()
    ]).then(([evaluationData, ranks, actions]) => {
        try {
            const form = document.querySelector("form");

            if (form && evaluationId) {
                form.action = form.action.replace("${SESSION_ID}", evaluationId);
            }

            if (evaluationData) {
                renderMissionHosts(evaluationData);
                renderPlayers(evaluationData);
            }

            if (ranks) {
                renderRanks(ranks);
            }

            if (actions) {
                renderActions(actions);
            }

        } catch (error) {
            console.error('Błąd podczas przetwarzania danych:', error);
        }
    }).catch(err => console.error('Błąd podczas pobierania:', err));
});

function getEvaluationIdFromUrl() {
    const pathName = window.location.pathname;
    return pathName.split("/").pop();
}

async function getEvaluationData(evaluationId) {
    const url = API_URL + `/evaluation/${evaluationId}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Response status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }
}

async function getRanksData() {
    const url = API_URL + `/ranks`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Error fetching ranks: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }
}

async function getActionsData() {
    const url = API_URL + `/actions`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Error fetching actions: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }
}

function renderMissionHosts(data) {
    const hostsContainer = document.querySelector('.missionHosts');
    if (data.missionMaker) {
        const mm = data.missionMaker;
        const mmDiv = document.createElement('div');
        mmDiv.classList.add('mission-maker');
        mmDiv.innerHTML = `Mission Maker: <span>${mm.name}</span>`;
        hostsContainer.appendChild(mmDiv);
    }
    if (Array.isArray(data.gameMasters) && data.gameMasters.length > 0) {
        data.gameMasters.forEach(gm => {
            const gmDiv = document.createElement('div');
            gmDiv.classList.add('game-master');
            gmDiv.innerHTML = `Game Master: <span>${gm.name}</span>`;
            hostsContainer.appendChild(gmDiv);
        });
    }
}

function renderPlayers(data) {
    const usersContainer = document.querySelector('.users-container');
    if (Array.isArray(data.players)) {
        data.players.forEach(player => {
            const playerDiv = document.createElement('div');
            playerDiv.classList.add('player-card');
            playerDiv.innerHTML = `
                <div class="player-info">
                    <img src="${player.avatarUrl}" alt="${player.name}-avatar" />
                    <div class="user-info"><span class="player-name">${player.name}</span>
                    <span class="player-id">ID: ${player.id}</span>
                    <div class="progress-bar">

                            <span alt="exp" class="exp">${player.exp}</span>
                        </div>
                    </div>
                </div>
                <div class="actions-container"></div>

                <input type="hidden" name="userId" value="${player.id}">
            `;
            usersContainer.appendChild(playerDiv);
        });
    }
}

function renderRanks(ranks) {
    const ranksContainer = document.querySelector('.ranks-container');
    if (!ranksContainer) return;
    if (Array.isArray(ranks)) {
        ranks.forEach(rank => {
            const rankDiv = document.createElement('div');
            rankDiv.classList.add('rank-item');
            rankDiv.textContent = `${rank.name} (EXP: ${rank.exp})`;
            ranksContainer.appendChild(rankDiv);
        });
    }
}

function renderActions(actions) {
    const actionsContainers = document.querySelectorAll('.actions-container');
    if (!actionsContainers || actionsContainers.length === 0) return;

    actionsContainers.forEach(actionsContainer => {
        for (const category in actions) {
            const categoryDiv = document.createElement('div');
            categoryDiv.classList.add('action-category');

            const heading = document.createElement('h3');
            heading.textContent = category;
            categoryDiv.appendChild(heading);

            const ul = document.createElement('ul');
            actions[category].forEach(actionItem => {
                const li = document.createElement('li');
                const isPositive = actionItem.value > 0;
                li.classList.add(isPositive ? 'appraisal' : 'reprimand');

                const input = document.createElement('input');
                input.type = 'checkbox';
                input.name = 'actions';
                input.value = actionItem.name;

                const label = document.createElement('label');

                // const user1 =
                // {
                //     "user_id": "21321312312",
                //     "actions": [
                //         "MAIN_OBJECTIVE",
                //         "KILLING_FRIENDS"
                //     ]
                // };
                //
                // const user2 =
                //     {
                //         "user_id": "21321312312",
                //         "actions": [
                //             "MAIN_OBJECTIVE",
                //             "KILLING_FRIENDS"
                //         ]
                //     };
                //
                // const response = {
                //     "users": [
                //         user1,
                //         user2
                //     ]
                // };

                label.innerHTML = ` ${actionItem.name} <span>${actionItem.value}XP</span>`;
                label.prepend(input);

                li.appendChild(label);
                ul.appendChild(li);
            });
            categoryDiv.appendChild(ul);
            actionsContainer.appendChild(categoryDiv);
        }
    });
}
