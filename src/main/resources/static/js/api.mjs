import { API_URL } from './environment.mjs';
import { getEvaluationIdFromUrl } from "./utils.mjs";

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
        throw error;
    }
}