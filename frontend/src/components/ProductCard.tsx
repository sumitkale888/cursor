import React from 'react';
import {
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {Product} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency} from '../utils/format';

interface ProductCardProps {
  product: Product;
  onPress: () => void;
  onAddToCart?: () => void;
  compact?: boolean;
}

export function ProductCard({
  product,
  onPress,
  onAddToCart,
  compact = false,
}: ProductCardProps) {
  return (
    <TouchableOpacity
      style={[styles.card, compact && styles.cardCompact]}
      onPress={onPress}
      activeOpacity={0.85}>
      <View style={[styles.imageWrap, compact && styles.imageWrapCompact]}>
        {product.imageUrl ? (
          <Image source={{uri: product.imageUrl}} style={styles.image} />
        ) : (
          <View style={styles.placeholder}>
            <Text style={styles.placeholderText}>🛒</Text>
          </View>
        )}
      </View>
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={2}>
          {product.name}
        </Text>
        <Text style={styles.category} numberOfLines={1}>
          {product.categoryName}
        </Text>
        <View style={styles.footer}>
          <Text style={styles.price}>{formatCurrency(product.price)}</Text>
          {onAddToCart ? (
            <TouchableOpacity
              style={styles.addBtn}
              onPress={e => {
                e.stopPropagation?.();
                onAddToCart();
              }}>
              <Text style={styles.addBtnText}>ADD</Text>
            </TouchableOpacity>
          ) : null}
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    overflow: 'hidden',
    flex: 1,
    margin: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cardCompact: {
    maxWidth: '48%',
  },
  imageWrap: {
    height: 120,
    backgroundColor: colors.primaryLight,
  },
  imageWrapCompact: {
    height: 100,
  },
  image: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  placeholder: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  placeholderText: {
    fontSize: 32,
  },
  info: {
    padding: spacing.md,
  },
  name: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  category: {
    fontSize: 12,
    color: colors.textLight,
    marginBottom: spacing.sm,
  },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  price: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  addBtn: {
    backgroundColor: colors.primaryLight,
    borderWidth: 1,
    borderColor: colors.primary,
    borderRadius: radius.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  addBtnText: {
    color: colors.primary,
    fontSize: 12,
    fontWeight: '700',
  },
});
