import { calculatePlayerXpForUser, getEvaluationIdFromUrl, updatePlayerXP } from './utils.mjs';
import { updatePlayerProgress, updateSidebar, renderMissionHosts, renderPlayers, renderRanks, renderActions } from './dom.mjs';
import { sendEvaluationData, getEvaluationData, getRanksData, getActionsData } from './api.mjs';
import { findPlayerRank } from './ranks.mjs';
import { API_URL } from './environment.mjs';

let ranks = [];
let actionsMap = {};
const selectedActionsMap = {};

document.addEventListener("DOMContentLoaded", async () => {
    const evaluationId = getEvaluationIdFromUrl();

    try {
        const [evaluationData, fetchedRanks, actions] = await Promise.all([
            getEvaluationData(evaluationId),
            getRanksData(),
            getActionsData()
        ]);

        if (!evaluationData) {
                    showEvaluationNotFound();
                    return;
        }

        ranks = fetchedRanks;

        if (actions) {
            for (const category in actions) {
                actions[category].forEach(actionItem => {
                    actionsMap[actionItem.name] = actionItem;
                });
            }
        }

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

            const playerCards = document.querySelectorAll('.player-card');
            playerCards.forEach(card => {
                const initialXp = parseInt(card.dataset.initialXp, 10);
                const { currentRank, currentIndex } = findPlayerRank(ranks, initialXp);
                card.dataset.initialRankIndex = currentIndex;
                updatePlayerProgress(card, ranks, initialXp);
            });
        }

        setupFormSubmitListener();

    } catch (error) {
              console.error('Error processing data:', error);
              if (error.status === 404) {
                  showEvaluationNotFound();
              }
          }
      });


export function setupFormSubmitListener() {
    const form = document.querySelector('form');
    if (!form) return;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const payload = preparePayload();
        if (!payload.missionName) {
            alert("Mission name is required!");
            return;
        }

        try {
            const response = await sendEvaluationData(payload);
            if (response && response.ok) {  // Check if response exists and is ok
                showFinalSummary();
            } else {
                throw new Error("Failed to submit evaluation");
            }
        } catch (error) {
            console.error("Błąd wysyłania oceny:", error);
            alert("Wystąpił błąd przy kończeniu oceniania.");
        }
    });

    form.addEventListener('change', function(e) {
        if (e.target.name === 'actions') {
            const playerCard = e.target.closest('.player-card');
            if (playerCard) {
                const userId = playerCard.querySelector('input[name="userId"]').value;
                const newXp = updatePlayerXP(playerCard, actionsMap);
                updatePlayerProgress(playerCard, ranks, newXp);

                const name = playerCard.dataset.name;
                const avatarUrl = playerCard.dataset.avatarUrl;
                const checkedActions = Array.from(playerCard.querySelectorAll('input[name="actions"]:checked'))
                    .map(i => i.value);

                let totalXpChange = 0;
                checkedActions.forEach(actionName => {
                    totalXpChange += actionsMap[actionName].value;
                });

                selectedActionsMap[userId] = {
                    name,
                    avatarUrl,
                    actions: checkedActions,
                    totalXpChange
                };

                updateSidebar(selectedActionsMap, actionsMap);
            }
        }
    });
}

export function preparePayload() {
    const usersData = [];
    const userCards = document.querySelectorAll('.player-card');

    userCards.forEach(card => {
        const userIdInput = card.querySelector('input[name="userId"]');
        if (!userIdInput) return;

        const userId = userIdInput.value;
        const checkedActions = Array.from(card.querySelectorAll('input[name="actions"]:checked'))
            .map(input => input.value);

        usersData.push({
            "user_id": userId,
            "actions": checkedActions
        });
    });
    const missionName = document.getElementById("missionTitle").value;

    return {
        "missionName": missionName,
        "users": usersData
    };
}

function showFinalSummary() {
    const form = document.querySelector('form');
    const header = document.querySelector('header');
    if (form) {
        form.style.display = 'none';
    }
    if (header) {
        header.style.display = 'none';
    }

    const summaryContainer = document.getElementById('finalSummaryContainer');
    if (summaryContainer) {
        summaryContainer.style.display = 'block';
        summaryContainer.innerHTML = '<h1>Zakończono ocenianie</h1>';

        const cardsContainer = document.createElement('div');
        cardsContainer.className = 'users-container'; // Reuse existing container class

        const playerCards = document.querySelectorAll('.player-card');
        playerCards.forEach(originalCard => {
            const name = originalCard.dataset.name;
            const avatarUrl = originalCard.dataset.avatarUrl;
            const initialXp = parseInt(originalCard.dataset.initialXp, 10);
            const currentXp = parseInt(originalCard.dataset.currentXp, 10);
            const diff = currentXp - initialXp;

            // Find ranks for both initial and current XP
            const initialRank = findPlayerRank(ranks, initialXp).currentRank;
            const currentRank = findPlayerRank(ranks, currentXp).currentRank;
            const nextRank = findPlayerRank(ranks, currentXp).nextRank;

            // Calculate progress percentage for the exp bar
            const percentage = calculatePercentageWithinRange(
                currentXp,
                currentRank.exp,
                nextRank ? nextRank.exp : null
            );

            const summaryCard = document.createElement('div');
            summaryCard.className = 'player-card';
            summaryCard.innerHTML = `
                <div class="user-info">
                    <div class="avatar-container">
                        <img src="${avatarUrl}" alt="${name}" class="avatar">
                    </div>
                    <div class="user-details">
                        <h3>${name}</h3>
                        <div class="rank-info">
                            ${initialRank.name !== currentRank.name ?
                                `<span class="previous-rank">${initialRank.name}</span> → ` :
                                ''
                            }
                            <span class="current-rank">${currentRank.name}</span>
                        </div>
                    </div>
                </div>

                <div class="exp-section">
                    <div class="exp-bar">
                        <div class="exp-bar-fill" style="width: ${percentage}%"></div>
                    </div>
                    <div class="exp-info">
                        <span class="current-exp">${currentXp} XP</span>
                        <span class="exp-change ${diff >= 0 ? 'positive' : 'negative'}">
                            ${diff >= 0 ? '+' : ''}${diff} XP
                        </span>
                        ${nextRank ? `<span class="next-rank">→ ${nextRank.name}: ${nextRank.exp} XP</span>` : ''}
                    </div>
                </div>

                <div class="actions-summary">
                    <h4>Wykonane akcje:</h4>
                    <ul>
                        ${Array.from(originalCard.querySelectorAll('input[name="actions"]:checked'))
                            .map(input => {
                                const actionValue = actionsMap[input.value].value;
                                return `<li>
                                    ${input.value}
                                    <span class="action-value">(+${actionValue} XP)</span>
                                </li>`;
                            })
                            .join('')
                        }
                    </ul>
                </div>
            `;

            cardsContainer.appendChild(summaryCard);
        });

        summaryContainer.appendChild(cardsContainer);
    }
}

async function cancelEvaluation() {
    if (!confirm("Czy na pewno chcesz przerwać ocenianie?")) {
        return;
    }
    const evaluationId = getEvaluationIdFromUrl();

    try {
        const response = await fetch(`${API_URL}/evaluation/${evaluationId}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`Nie udało się usunąć oceny, status: ${response.status}`);
        }

        const form = document.querySelector('form');
        const header = document.querySelector('header');  // Add this line
        if (form) {
            form.style.display = 'none';
        }
        if (header) {  // Add this block
            header.style.display = 'none';
        }

        const cancelledContainer = document.getElementById('cancelledContainer');
        if (cancelledContainer) {
            cancelledContainer.style.display = 'block';
        }
    } catch (err) {
        console.error("Błąd anulowania oceniania:", err);
        alert("Wystąpił błąd przy anulowaniu oceniania.");
    }
}

function showEvaluationNotFound() {
    const form = document.querySelector('form');
    const header = document.querySelector('header');
    if (form) form.style.display = 'none';
    if (header) header.style.display = 'none';

    // Create and append error message if it doesn't exist
    if (!document.getElementById('evaluationNotFound')) {
        const errorContainer = document.createElement('div');
        errorContainer.id = 'evaluationNotFound';
        errorContainer.innerHTML = `
            <h1>Sesja oceny nie istnieje</h1>
            <p>Wybrana sesja oceny nie istnieje lub została zakończona.</p>
        `;
        document.body.appendChild(errorContainer);
    }
}

window.cancelEvaluation = cancelEvaluation;
