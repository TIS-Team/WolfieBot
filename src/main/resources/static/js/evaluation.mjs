// evaluation.mjs
import { getEvaluationData, getRanksData, getActionsData, sendEvaluationData, preparePayload } from './api.mjs';
import { renderMissionHosts, renderPlayers, renderRanks, renderActions, updatePlayerProgress } from './dom.mjs';
import { getEvaluationIdFromUrl } from './utils.mjs';
import { findPlayerRank } from './ranks.mjs';
import { API_URL } from './environment.mjs';

import { cancelEvaluationApi } from './api.mjs';

async function cancelEvaluation() {
    await cancelEvaluationApi(evaluationId);
}
window.cancelEvaluation = cancelEvaluation;



document.addEventListener("DOMContentLoaded", async () => {
    const evaluationId = getEvaluationIdFromUrl();

    try {

        const [evaluationData, ranks, actions] = await Promise.all([
            getEvaluationData(evaluationId),
            getRanksData(),
            getActionsData()
        ]);


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


            const actionsMap = {};
            for (const category in actions) {
                actions[category].forEach(actionItem => {
                    actionsMap[actionItem.name] = actionItem;
                });
            }


            const playerCards = document.querySelectorAll('.player-card');
            playerCards.forEach(card => {
                const initialXp = parseInt(card.dataset.initialXp, 10);
                const { currentRank, currentIndex } = findPlayerRank(ranks, initialXp);
                card.dataset.initialRankIndex = currentIndex;
                updatePlayerProgress(card, ranks, initialXp);
            });


            if (form) {
                form.addEventListener('submit', async (evt) => {
                    evt.preventDefault();
                    try {
                        const payload = preparePayload();
                        await sendEvaluationData(payload);
                        showFinalSummary();
                    } catch (error) {
                        console.error("Błąd wysyłania oceny:", error);
                        alert("Wystąpił błąd przy kończeniu oceniania.");
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error processing data:', error);
    }
});


function showFinalSummary() {
    // chowamy formularz
    const form = document.querySelector('form');
    if (form) {
        form.style.display = 'none';
    }

    const summaryContainer = document.getElementById('finalSummaryContainer');
    if (summaryContainer) {
        summaryContainer.style.display = 'block';

        const xpList = document.getElementById('xpSummaryList');
        if (xpList) {
            xpList.innerHTML = '';

            const playerCards = document.querySelectorAll('.player-card');
            playerCards.forEach(card => {
                const name = card.dataset.name;
                const initialXp = parseInt(card.dataset.initialXp, 10);
                const currentXp = parseInt(card.dataset.currentXp, 10);
                const diff = currentXp - initialXp;

                const li = document.createElement('li');
                li.innerHTML = `
                  <strong>${name}</strong>
                  – otrzymane XP:
                  <span style="color:${diff >= 0 ? 'green' : 'red'};">
                    ${diff >= 0 ? '+' : ''}${diff}
                  </span>
                  (z ${initialXp} na ${currentXp})
                `;
                xpList.appendChild(li);
            });
        }
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
        if (form) {
            form.style.display = 'none';
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

window.cancelEvaluation = cancelEvaluation;
