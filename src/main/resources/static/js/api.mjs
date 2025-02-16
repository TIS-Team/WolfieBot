import {API_URL} from './environment.mjs';
import {getEvaluationIdFromUrl} from "./utils.mjs";

export async function cancelEvaluationApi(evaluationId) {
    const url = `${API_URL}/evaluation/${evaluationId}`;
    const resp = await fetch(url, { method: 'DELETE' });
    if (!resp.ok) {
        throw new Error(`Nie udało się usunąć oceny, status: ${resp.status}`);
    }
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

export async function getEvaluationData(evaluationId) {
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

export async function getRanksData() {
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

export async function getActionsData() {
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

export async function sendEvaluationData(data) {
    const evaluationId = getEvaluationIdFromUrl();
    const url = `${API_URL}/evaluation/${evaluationId}`;
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            throw new Error(`Failed to send data: ${response.status}`);
        }
    } catch (error) {
        console.error(error);
    }
}