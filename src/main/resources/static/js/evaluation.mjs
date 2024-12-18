import { getEvaluationData, getRanksData, getActionsData } from './api.mjs';
import { renderMissionHosts, renderPlayers, renderRanks, renderActions, updatePlayerProgress } from './dom.mjs';
import { getEvaluationIdFromUrl } from './utils.mjs';
import { setupFormSubmitListener } from './events.mjs';
import { findPlayerRank } from './ranks.mjs';



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
                    card.dataset.initialRankIndex = currentIndex; // Store the initial rank index
                    updatePlayerProgress(card, ranks, initialXp);
                });

                setupFormSubmitListener(ranks, actionsMap);


            }

    } catch (error) {
        console.error('Error processing data:', error);
    }
});
