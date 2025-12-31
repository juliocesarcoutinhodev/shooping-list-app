import * as React from 'react';
import Svg, { Circle, Line, Path, Rect } from 'react-native-svg';

export default function EmptyBoxSvg({ width = 160, height = 120 }) {
  return (
    <Svg width={width} height={height} viewBox='0 0 160 120' fill='none'>
      {/* Caixa principal */}
      <Rect
        x='30'
        y='50'
        width='100'
        height='50'
        rx='8'
        fill='#F9FAF7'
        stroke='#E0E0E0'
        strokeWidth='2'
      />
      {/* Tampa esquerda */}
      <Path d='M30 50 L60 30 L80 50 Z' fill='#FFD580' />
      {/* Tampa direita */}
      <Path d='M130 50 L100 30 L80 50 Z' fill='#FFB74D' />
      {/* Face triste */}
      <Path d='M60 80 Q65 85 70 80' stroke='#E57373' strokeWidth='2' fill='none' />
      <Circle cx='55' cy='75' r='2' fill='#E57373' />
      <Circle cx='75' cy='75' r='2' fill='#E57373' />
      {/* Alface/cebolinha */}
      <Line x1='120' y1='50' x2='130' y2='35' stroke='#81C784' strokeWidth='3' />
      <Line x1='125' y1='50' x2='135' y2='40' stroke='#388E3C' strokeWidth='3' />
    </Svg>
  );
}
