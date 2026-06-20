import React, {useCallback, useEffect} from 'react';
import {
  Alert,
  FlatList,
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {Button} from '../components/Button';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {useCart} from '../context/CartContext';
import {CartStackParamList} from '../navigation/types';
import {CartItem} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency} from '../utils/format';

type Props = NativeStackScreenProps<CartStackParamList, 'Cart'>;

export function CartScreen({navigation}: Props) {
  const {cart, isLoading, error, refreshCart, updateQuantity, removeItem} =
    useCart();

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const handleUpdateQty = useCallback(
    async (item: CartItem, delta: number) => {
      const newQty = item.quantity + delta;
      if (newQty < 1) {
        return;
      }
      try {
        await updateQuantity(item.productId, newQty);
      } catch (err) {
        Alert.alert('Error', (err as Error).message);
      }
    },
    [updateQuantity],
  );

  const handleRemove = useCallback(
    async (productId: number) => {
      try {
        await removeItem(productId);
      } catch (err) {
        Alert.alert('Error', (err as Error).message);
      }
    },
    [removeItem],
  );

  if (isLoading && !cart?.items.length) {
    return <LoadingSpinner fullScreen />;
  }

  if (error && !cart?.items.length) {
    return <ErrorView message={error} onRetry={refreshCart} />;
  }

  const items = cart?.items ?? [];
  const isEmpty = items.length === 0;

  const renderItem = ({item}: {item: CartItem}) => (
    <View style={styles.item}>
      <View style={styles.itemImageWrap}>
        {item.productImageUrl ? (
          <Image
            source={{uri: item.productImageUrl}}
            style={styles.itemImage}
          />
        ) : (
          <Text style={styles.itemPlaceholder}>🛒</Text>
        )}
      </View>
      <View style={styles.itemInfo}>
        <Text style={styles.itemName} numberOfLines={2}>
          {item.productName}
        </Text>
        <Text style={styles.itemPrice}>{formatCurrency(item.price)}</Text>
        <View style={styles.qtyRow}>
          <TouchableOpacity
            style={styles.qtyBtn}
            onPress={() => handleUpdateQty(item, -1)}>
            <Text style={styles.qtyBtnText}>−</Text>
          </TouchableOpacity>
          <Text style={styles.qty}>{item.quantity}</Text>
          <TouchableOpacity
            style={styles.qtyBtn}
            onPress={() => handleUpdateQty(item, 1)}>
            <Text style={styles.qtyBtnText}>+</Text>
          </TouchableOpacity>
        </View>
      </View>
      <View style={styles.itemRight}>
        <Text style={styles.subtotal}>{formatCurrency(item.subtotal)}</Text>
        <TouchableOpacity onPress={() => handleRemove(item.productId)}>
          <Text style={styles.remove}>Remove</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <Header
        title="My Cart"
        subtitle={
          isEmpty
            ? 'Your cart is empty'
            : `${cart?.totalItems ?? 0} items`
        }
      />
      {isEmpty ? (
        <View style={styles.emptyWrap}>
          <Text style={styles.emptyIcon}>🛒</Text>
          <Text style={styles.emptyTitle}>Cart is empty</Text>
          <Text style={styles.emptyText}>
            Add groceries from home to get started
          </Text>
        </View>
      ) : (
        <>
          <FlatList
            data={items}
            keyExtractor={item => String(item.id)}
            renderItem={renderItem}
            contentContainerStyle={styles.list}
          />
          <View style={styles.footer}>
            <View style={styles.totalRow}>
              <Text style={styles.totalLabel}>Total</Text>
              <Text style={styles.totalValue}>
                {formatCurrency(cart?.totalAmount ?? 0)}
              </Text>
            </View>
            <Button
              title="Proceed to Checkout"
              onPress={() => navigation.navigate('Checkout')}
            />
          </View>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  list: {
    padding: spacing.lg,
    paddingBottom: 120,
  },
  item: {
    flexDirection: 'row',
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  itemImageWrap: {
    width: 64,
    height: 64,
    borderRadius: radius.md,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  itemImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  itemPlaceholder: {
    fontSize: 24,
  },
  itemInfo: {
    flex: 1,
    marginLeft: spacing.md,
  },
  itemName: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  itemPrice: {
    fontSize: 13,
    color: colors.textSecondary,
    marginBottom: spacing.sm,
  },
  qtyRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  qtyBtn: {
    width: 28,
    height: 28,
    borderRadius: radius.sm,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
  },
  qtyBtnText: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.primary,
  },
  qty: {
    marginHorizontal: spacing.md,
    fontSize: 15,
    fontWeight: '600',
    color: colors.text,
  },
  itemRight: {
    alignItems: 'flex-end',
    justifyContent: 'space-between',
  },
  subtotal: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  remove: {
    fontSize: 12,
    color: colors.error,
    fontWeight: '600',
  },
  footer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.surface,
    padding: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: spacing.md,
  },
  totalLabel: {
    fontSize: 16,
    color: colors.textSecondary,
  },
  totalValue: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.text,
  },
  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.xxl,
  },
  emptyIcon: {
    fontSize: 56,
    marginBottom: spacing.lg,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  emptyText: {
    fontSize: 14,
    color: colors.textSecondary,
    textAlign: 'center',
  },
});
