import React from 'react';
import {StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import {Category} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';

const CATEGORY_ICONS: Record<string, string> = {
  'Fruits & Vegetables': '🥬',
  'Dairy & Breakfast': '🥛',
  'Snacks & Munchies': '🍿',
  'Cold Drinks & Juices': '🥤',
};

interface CategoryCardProps {
  category: Category;
  onPress: () => void;
}

export function CategoryCard({category, onPress}: CategoryCardProps) {
  const icon = CATEGORY_ICONS[category.name] ?? '🛍️';

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.85}>
      <View style={styles.iconWrap}>
        <Text style={styles.icon}>{icon}</Text>
      </View>
      <Text style={styles.name} numberOfLines={2}>
        {category.name}
      </Text>
      {category.productCount != null ? (
        <Text style={styles.count}>{category.productCount} items</Text>
      ) : null}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.lg,
    alignItems: 'center',
    flex: 1,
    margin: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
    minWidth: '45%',
  },
  iconWrap: {
    width: 56,
    height: 56,
    borderRadius: radius.full,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.md,
  },
  icon: {
    fontSize: 28,
  },
  name: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
    textAlign: 'center',
    marginBottom: spacing.xs,
  },
  count: {
    fontSize: 12,
    color: colors.textLight,
  },
});
