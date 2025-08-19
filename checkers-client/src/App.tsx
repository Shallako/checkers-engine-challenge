import React, { useCallback, useMemo, useRef, useState } from 'react';
import { createGame, postMove } from './api';
import type { GameResponseDto, MoveRequestDto, NewGameRequest, PlayerDto, PlayerColor, Position } from './types';
import Board from './components/Board';

function keyFor(row: number, column: number) {
  return `Position[row=${row}, column=${column}]`;
}

function cloneBoard(board: GameResponseDto['board']): GameResponseDto['board'] {
  return { size: board.size, pieces: { ...board.pieces } };
}

export default function App() {
  const [game, setGame] = useState<GameResponseDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [moving, setMoving] = useState(false);
  const [selected, setSelected] = useState<Position | null>(null);
  const lastHumanMoveRef = useRef<{ from: Position; to: Position } | null>(null);

  const humanPlayer: PlayerDto | null = useMemo(() => {
    if (!game) return null;
    return game.redPlayer.type === 'HUMAN' ? game.redPlayer : game.blackPlayer.type === 'HUMAN' ? game.blackPlayer : null;
  }, [game]);

  const computerPlayer: PlayerDto | null = useMemo(() => {
    if (!game) return null;
    return game.redPlayer.type === 'COMPUTER' ? game.redPlayer : game.blackPlayer.type === 'COMPUTER' ? game.blackPlayer : null;
  }, [game]);

  const canMove = !!game && !game.isGameOver && humanPlayer && game.currentTurn === humanPlayer.color && !moving;

  const onNewGame = async (req: NewGameRequest) => {
    setError(null);
    setCreating(true);
    try {
      const g = await createGame(req);
      setGame(g);
      setSelected(null);
    } catch (e: any) {
      setError(e.message || String(e));
    } finally {
      setCreating(false);
    }
  };

  const onSelect = useCallback(async (pos: Position) => {
    if (!game || !humanPlayer) return;

    const piece = game.board.pieces[keyFor(pos.row, pos.column)];
    if (!selected) {
      // Selecting a source square: must have a human piece
      if (piece && piece.color === humanPlayer.color) {
        setSelected(pos);
      }
      return;
    } else {
      // Selecting destination
      const from = selected;
      const to = pos;
      if (from.row === to.row && from.column === to.column) {
        setSelected(null);
        return;
      }

      // If it's not our turn yet, allow selecting but do not perform a move
      if (!canMove) {
        setSelected(null);
        return;
      }

      setSelected(null);
      setMoving(true);

      const moveReq: MoveRequestDto = {
        gameId: game.id,
        playerId: humanPlayer.id,
        from,
        to,
      };

      try {
        const afterMove = await postMove(moveReq);
        setGame(afterMove);
      } catch (e: any) {
        setError(e.message || String(e));
      } finally {
        setMoving(false);
      }
    }
  }, [game, humanPlayer, selected, canMove, computerPlayer]);

  return (
    <div className="container">
      <header>
        <h1>Checkers</h1>
      </header>

      <div className="panel" style={{ marginBottom: 16 }}>
        <NewGameForm onSubmit={onNewGame} loading={creating} />
      </div>

      {game && (
        <div className="flex">
          <div className="panel" style={{ flex: 1 }}>
            <h3 style={{ marginTop: 0 }}>Game ID: {game.id}</h3>
            <div>
              <strong>Turn:</strong> {game.currentPlayerName} ({game.currentTurn})
            </div>
            <div className="status">
              {game.isGameOver ? 'Game Over' : canMove ? 'Your move' : 'Waiting...'}
            </div>
            <div style={{ marginTop: 12 }}>
              <Board
                board={game.board}
                selectableColor={humanPlayer ? humanPlayer.color : null}
                onSelect={onSelect}
                highlight={selected}
                perspective={humanPlayer?.color}
              />
            </div>
          </div>
          <div className="panel" style={{ width: 320 }}>
            <h3 style={{ marginTop: 0 }}>Players</h3>
            <p>Red: {game.redPlayer.name} ({game.redPlayer.type})</p>
            <p>Black: {game.blackPlayer.name} ({game.blackPlayer.type})</p>
            <p><strong>Board:</strong> {game.board.size}</p>
          </div>
        </div>
      )}

      {error && <div className="panel error" style={{ marginTop: 16 }}>{error}</div>}
    </div>
  );
}

function NewGameForm({ onSubmit, loading }: { onSubmit: (req: NewGameRequest) => void | Promise<void>; loading: boolean; }) {
  const [playerName, setPlayerName] = useState('Player');
  const [boardSize, setBoardSize] = useState<NewGameRequest['boardSize']>('EIGHT_BY_EIGHT');
  const [playerColor, setPlayerColor] = useState<PlayerColor>('RED');

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit({ playerName, boardSize, playerColor }); }}>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
        <label>
          Name
          <input type="text" value={playerName} onChange={e => setPlayerName(e.target.value)} />
        </label>
        <label>
          Board Size
          <select value={boardSize} onChange={e => setBoardSize(e.target.value as any)}>
            <option value="EIGHT_BY_EIGHT">8 x 8</option>
            <option value="TEN_BY_TEN">10 x 10</option>
          </select>
        </label>
        <label>
          Your Color
          <select value={playerColor} onChange={e => setPlayerColor(e.target.value as any)}>
            <option value="RED">Red</option>
            <option value="BLACK">Black</option>
          </select>
        </label>
        <button type="submit" disabled={loading}>{loading ? 'Creating...' : 'New Game'}</button>
      </div>
    </form>
  );
}
