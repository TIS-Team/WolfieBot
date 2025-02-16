export function getEvaluationIdFromUrl() {
    const pathName = window.location.pathname;
    return pathName.split("/").pop();
}



export function calculatePercentageWithinRange(value, min, max) {
    if (max === null) {
        return 100;
    }
    const range = max - min;
    if (range <= 0) return 0;
    const relative = (value - min) / range;
    return Math.max(0, Math.min(100, relative * 100));
}


export function calculatePlayerXpForUser(playerCard, actionsMap) {
    const initialXp = parseInt(playerCard.dataset.initialXp, 10);
    const checkedActions = Array.from(playerCard.querySelectorAll('input[name="actions"]:checked'));

    let addedXp = 0;
    checkedActions.forEach(actionInput => {
        const actionName = actionInput.value;
        const actionValue = actionsMap[actionName].value;
        addedXp += actionValue;
    });

    return initialXp + addedXp;
}

