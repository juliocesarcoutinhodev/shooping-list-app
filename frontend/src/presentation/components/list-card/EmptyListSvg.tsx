import * as React from 'react';
import Svg, { Ellipse, Path, Rect } from 'react-native-svg';

export default function EmptyListSvg({ width = 160, height = 120 }) {
  return (
    <Svg width={width} height={height} viewBox='0 0 160 120' fill='none'>
      {/* Papel da lista */}
      <Rect
        x='50'
        y='30'
        width='60'
        height='70'
        rx='12'
        fill='#fff'
        stroke='#E0E0E0'
        strokeWidth='2'
      />
      {/* Linhas da lista */}
      <Path d='M60 50 h40' stroke='#A5D6A7' strokeWidth='3' strokeLinecap='round' />
      <Path d='M60 60 h40' stroke='#A5D6A7' strokeWidth='3' strokeLinecap='round' />
      <Path d='M60 70 h28' stroke='#BDBDBD' strokeWidth='2' strokeLinecap='round' />
      <Path d='M60 80 h24' stroke='#BDBDBD' strokeWidth='2' strokeLinecap='round' />
      {/* Check verde */}
      <Path
        d='M70 90 l6 6 l14 -14'
        stroke='#2ECC71'
        strokeWidth='3'
        strokeLinecap='round'
        fill='none'
      />
      {/* Sombra */}
      <Ellipse cx='80' cy='108' rx='28' ry='6' fill='#F1F8E9' />
    </Svg>
  );
}
