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
  perspective?: PlayerColor;
}

export default function Board({ board, selectableColor, onSelect, highlight, perspective = 'RED' }: BoardProps) {
  const rows = boardSizeToRows(board.size);
  const cols = boardSizeToCols(board.size);

  const rowIndexes = Array.from({ length: rows }).map((_, i) => i);
  if (perspective === 'BLACK') {
    rowIndexes.reverse();
  }

  return (
    <div className="board" style={{ gridTemplateColumns: `repeat(${cols}, 56px)` }}>
      {rowIndexes.map(r => (
        Array.from({ length: cols }).map((_, c) => {
          const piece = board.pieces[keyFor(r, c)];
          const dark = isDark(r, c);
          const selectable = piece && piece.color === selectableColor;
          const isHL = highlight && highlight.row === r && highlight.column === c;
          return (
            <div
              key={`${r}-${c}`}
              className={`square ${dark ? 'dark' : 'light'} ${isHL ? 'highlight' : ''}`}
              onClick={() => {
                let selectedRow = r;
                if (perspective === 'BLACK') {
                  selectedRow = rows - 1 - r;
                }
                const selectedPos = { row: selectedRow, column: c };
                onSelect(selectedPos);
              }}
              style={{ cursor: dark ? 'pointer' : 'default' }}
            >
              {dark && piece && (
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
