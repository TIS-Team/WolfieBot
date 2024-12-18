import { findPlayerRank } from './ranks.mjs';
import { calculatePercentageWithinRange } from './utils.mjs';
export function renderMissionHosts(data) {
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

export function renderPlayers(data) {
    const usersContainer = document.querySelector('.users-container');
    if (Array.isArray(data.players)) {
        data.players.forEach(player => {
            const playerDiv = document.createElement('div');
            playerDiv.classList.add('player-card');
            playerDiv.dataset.initialXp = player.exp;
            playerDiv.dataset.currentXp = player.exp;
            playerDiv.dataset.lastKnownRankIndex = '-1';
            playerDiv.dataset.name = player.name;
            playerDiv.dataset.avatarUrl = player.avatarUrl;

            playerDiv.innerHTML = `
                <div class="player-info">
                    <img class="player-avatar" src="${player.avatarUrl}" alt="${player.name}-avatar" />
                    <div class="user-info">
                        <span class="player-name">${player.name}</span>
                        <span class="player-id">ID: ${player.id}</span>
                        <div class="rank-info"></div>
                        <div class="progress-bar">
                            <div class="progress-baseline"></div>
                            <div class="progress-change"></div>
                            <span class="progress-text"></span>
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
export function updatePlayerProgress(playerCard, ranks, newXp) {
    const initialXp = parseInt(playerCard.dataset.initialXp, 10);
    const initialRankIndex = parseInt(playerCard.dataset.initialRankIndex, 10);

    playerCard.dataset.currentXp = newXp;

    const { currentRank, nextRank, previousRank, currentIndex } = findPlayerRank(ranks, newXp);
    const rankInfoDiv = playerCard.querySelector('.rank-info');

    let arrow = '';
    if (currentIndex > initialRankIndex) {
        arrow = '<span class="rank-arrow" style="color:#008000 !important;">↑</span>';
    } else if (currentIndex < initialRankIndex) {
        arrow = '<span class="rank-arrow" style="color:#a41013 !important;">↓</span>';
    }

    const rankName = currentRank ? currentRank.name : 'Brak rangi';
    rankInfoDiv.innerHTML = `Ranga: <span>${rankName} ${arrow}</span>`;

    const currentRankExp = currentRank ? currentRank.exp : 0;
    const nextRankExp = nextRank ? nextRank.exp : 0; // If no next rank, show 0 XP
    const baselinePercent = calculatePercentageWithinRange(initialXp, currentRankExp, nextRankExp || currentRankExp + 1000);
    const currentPercent = nextRank
        ? calculatePercentageWithinRange(newXp, currentRankExp, nextRankExp)
        : 100;

    const baselineEl = playerCard.querySelector('.progress-baseline');
    const changeEl = playerCard.querySelector('.progress-change');
    const progressText = playerCard.querySelector('.progress-text');

    baselineEl.style.left = '0%';
    baselineEl.style.width = `${baselinePercent}%`;
    baselineEl.style.backgroundColor = 'orange';

    if (newXp > initialXp) {
        const diff = currentPercent - baselinePercent;
        changeEl.style.left = `${baselinePercent}%`;
        changeEl.style.width = `${diff}%`;
        changeEl.style.backgroundColor = 'green';
    } else if (newXp < initialXp) {
        const diff = baselinePercent - currentPercent;
        changeEl.style.width = `${diff}%`;
        changeEl.style.left = `${baselinePercent - diff}%`;
        changeEl.style.backgroundColor = '#a41013';
    } else {
        changeEl.style.width = '0%';
        changeEl.style.left = `${baselinePercent}%`;
    }

    let nextXpText = nextRank ? `${nextRankExp} XP` : 'Max';
    progressText.textContent = `${newXp} XP / ${nextXpText}`;
}


export function updateSidebar(selectedActionsMap, actionsMap) {
    const sidebar = document.querySelector('.sidebar');
    sidebar.innerHTML = '';

    for (const userId in selectedActionsMap) {
        const userData = selectedActionsMap[userId];
        if (userData.actions.length === 0) continue; // No actions selected skip  user

        const userCard = document.createElement('div');
        userCard.classList.add('sidebar-user-card');

        userCard.innerHTML = `
            <div class="sidebar-user-info">
                <img class="sidebar-user-avatar" src="${userData.avatarUrl}" alt="${userData.name}-avatar" />
                <span class="sidebar-user-name">${userData.name}</span>
            </div>
            <ul class="sidebar-actions-list"></ul>
            <div class="sidebar-total">Total XP: <span class="sidebar-total-xp"></span></div>
        `;

        const actionsList = userCard.querySelector('.sidebar-actions-list');
        const totalXpSpan = userCard.querySelector('.sidebar-total-xp');
        const totalXpChange = userData.totalXpChange;

        const totalSign = totalXpChange > 0 ? '+' : (totalXpChange < 0 ? '' : '');
        if (totalXpChange > 0) {
            totalXpSpan.classList.add('appraisal');
        } else if (totalXpChange < 0) {
            totalXpSpan.classList.add('reprimand');
        }
        totalXpSpan.textContent = `${totalSign}${totalXpChange}XP`;

        userData.actions.forEach(actionName => {
            const li = document.createElement('li');
            const actionValue = actionsMap[actionName].value;
            const sign = actionValue > 0 ? '+' : ''; // If negative, the minus sign is included

            // Set class based on positive or negative
            if (actionValue > 0) {
                li.classList.add('appraisal');
            } else if (actionValue < 0) {
                li.classList.add('reprimand');
            }

            const actionDisplayName = actionsMap[actionName].displayName;
            const xpSpan = `<span class="xp-value">${sign}${actionValue}XP</span>`;

            li.innerHTML = `${actionDisplayName}: ${xpSpan}`;
            actionsList.appendChild(li);
        });

        sidebar.appendChild(userCard);
    }
}



export function renderRanks(ranks) {
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

export function renderActions(actions) {
    const actionsContainers = document.querySelectorAll('.actions-container');
    if (!actionsContainers || actionsContainers.length === 0) return;

    let actionInputCounter = 0;

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
                const inputId = `action_${actionInputCounter++}`;
                input.id = inputId;

                const label = document.createElement('label');
                label.setAttribute('for', inputId);
                label.innerHTML = `${actionItem.displayName} <span>${actionItem.value}XP</span>`;

                li.appendChild(input);
                li.appendChild(label);
                ul.appendChild(li);
            });
            categoryDiv.appendChild(ul);
            actionsContainer.appendChild(categoryDiv);
        }
    });
}
