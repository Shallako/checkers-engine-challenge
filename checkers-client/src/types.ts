// Types matching backend DTOs
export type PlayerColor = 'RED' | 'BLACK';
export type PieceType = 'MAN' | 'KING';
export type BoardSize = 'EIGHT_BY_EIGHT' | 'STANDARD' | 'TEN_BY_TEN' | 'INTERNATIONAL';

export interface NewGameRequest {
  playerName: string;
  boardSize: BoardSize;
  playerColor: PlayerColor;
}

export interface Position {
  row: number;
  column: number;
}

export interface MoveRequestDto {
  gameId: string;
  playerId: string;
  from: Position | null;
  to: Position | null;
}

export interface PieceDto {
  color: PlayerColor;
  type: PieceType;
}

export interface BoardDto {
  size: BoardSize; // could be object in backend, but enum label is enough here
  pieces: Record<string, PieceDto>; // key format: "Position[row=R, column=C]"
}

export interface PlayerDto {
  id: string;
  name: string;
  color: PlayerColor;
  type: 'HUMAN' | 'COMPUTER';
}

export interface GameResponseDto {
  id: string;
  board: BoardDto;
  redPlayer: PlayerDto;
  blackPlayer: PlayerDto;
  currentTurn: PlayerColor;
  state: string;
  createdAt: string;
  updatedAt: string;
  currentPlayerName: string;
  isGameOver: boolean;
}
