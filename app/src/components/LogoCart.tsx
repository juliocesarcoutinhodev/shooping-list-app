import React from 'react';
import Svg, { Circle, G, Path } from 'react-native-svg';
import { colors } from '../styles/colors';

export type LogoCartProps = {
  size?: number;
  color?: string;
};

export default function LogoCart({ size = 96, color = colors.primaryForeground }: LogoCartProps) {
  const stroke = color;
  const strokeWidth = 3;

  return (
    <Svg width={size} height={size} viewBox="0 0 96 96" fill="none">
      <G stroke={stroke} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
        {/* Handle */}
        <Path d="M16 22 L26 30" />
        {/* Upper rim */}
        <Path d="M26 30 H72" />
        {/* Basket body */}
        <Path d="M72 30 L64 54 H34 L26 30" />
        {/* Inner lines for style */}
        <Path d="M38 42 H60" opacity="0.7" />
        <Path d="M40 48 H56" opacity="0.7" />
        {/* Wheels */}
        <Circle cx="38" cy="62" r="6" />
        <Circle cx="60" cy="62" r="6" />
      </G>
    </Svg>
  );
}
