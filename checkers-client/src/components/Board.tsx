import React from 'react';
import type { BoardDto, PlayerColor, Position } from '../types';

function keyFor(row: number, column: number) {
  return `Position[row=${row}, column=${column}]`;
}

function isDark(row: number, col: number) {
  return (row + col) % 2 === 1;
}

export interface BoardProps {
  board: BoardDto;
  selectableColor: PlayerColor | null; // which color user can select/move
  onSelect: (pos: Position) => void;
  highlight?: Position | null;
}

export default function Board({ board, selectableColor, onSelect, highlight }: BoardProps) {
  const rows = boardSizeToRows(board.size);
  const cols = boardSizeToCols(board.size);

  return (
    <div className="board" style={{ gridTemplateColumns: `repeat(${cols}, 56px)` }}>
      {Array.from({ length: rows }).map((_, r) => (
        Array.from({ length: cols }).map((_, c) => {
          const piece = board.pieces[keyFor(r, c)];
          const dark = isDark(r, c);
          const selectable = piece && piece.color === selectableColor;
          const isHL = highlight && highlight.row === r && highlight.column === c;
          return (
            <div
              key={`${r}-${c}`}
              className={`square ${dark ? 'dark' : 'light'} ${isHL ? 'highlight' : ''}`}
              onClick={() => onSelect({ row: r, column: c })}
              style={{ cursor: dark ? 'pointer' : 'default', opacity: dark ? 1 : 0.8 }}
            >
              {piece && (
                <div
                  className={`piece ${piece.color === 'RED' ? 'red' : 'black'} ${piece.type === 'KING' ? 'king' : ''}`}
                  title={`${piece.color} ${piece.type}`}
                  style={{ outline: selectable ? '2px solid #4f46e5' : 'none' }}
                />
              )}
            </div>
          );
        })
      ))}
    </div>
  );
}

function boardSizeToRows(size: BoardDto['size']) {
  switch (size) {
    case 'TEN_BY_TEN':
    case 'INTERNATIONAL':
      return 10;
    case 'EIGHT_BY_EIGHT':
    case 'STANDARD':
    default:
      return 8;
  }
}
function boardSizeToCols(size: BoardDto['size']) { return boardSizeToRows(size); }
