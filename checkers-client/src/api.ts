import type { GameResponseDto, MoveRequestDto, NewGameRequest } from './types';

const BASE_URL = 'http://localhost:8080';

export async function createGame(req: NewGameRequest): Promise<GameResponseDto> {
  const res = await fetch(`${BASE_URL}/games`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function postMove(move: MoveRequestDto): Promise<GameResponseDto> {
  const res = await fetch(`${BASE_URL}/games/${encodeURIComponent(move.gameId)}/moves`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(move),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
