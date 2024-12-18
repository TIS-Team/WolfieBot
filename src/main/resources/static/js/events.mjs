import { preparePayload, calculatePlayerXpForUser } from './utils.mjs';
import { updatePlayerProgress, updateSidebar } from './dom.mjs';
import {sendEvaluationData} from './api.mjs';
let ranks = [];
let actionsMap = {};
const selectedActionsMap = {};

export function setupFormSubmitListener(passedRanks, passedActionsMap) {
    ranks = passedRanks;
    actionsMap = passedActionsMap;

    const form = document.querySelector('form');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        const payload = preparePayload();
        console.log("Payload:", JSON.stringify(payload, null, 2));
//        sendEvaluationData(payload);
    });

    form.addEventListener('change', function(e) {
        if (e.target.name === 'actions') {
            const playerCard = e.target.closest('.player-card');
            if (playerCard) {
                const userId = playerCard.querySelector('input[name="userId"]').value;
                const newXp = calculatePlayerXpForUser(playerCard, actionsMap);
                updatePlayerProgress(playerCard, ranks, newXp);

                const name = playerCard.dataset.name;
                const avatarUrl = playerCard.dataset.avatarUrl;
                const checkedActions = Array.from(playerCard.querySelectorAll('input[name="actions"]:checked')).map(i => i.value);

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
